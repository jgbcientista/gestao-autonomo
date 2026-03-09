/*
 * Chaincode para Auditoria de Autenticação
 * Este chaincode gerencia eventos de autenticação na blockchain Hyperledger Fabric
 */

'use strict';

const { Contract } = require('fabric-contract-api');

class AuthAuditContract extends Contract {

    // Inicializar o ledger
    async initLedger(ctx) {
        console.info('Inicializando chaincode de auditoria de autenticação');
        
        // Criar um evento inicial
        const initialEvent = {
            userId: 'system',
            userEmail: 'system@auth.com',
            eventType: 'SYSTEM_INIT',
            decision: 'ALLOWED',
            riskScore: 0.0,
            ipAddress: '127.0.0.1',
            location: 'System',
            dataHash: 'init_hash',
            timestamp: new Date().toISOString(),
            confirmations: 1
        };

        await ctx.stub.putState('INIT_EVENT', Buffer.from(JSON.stringify(initialEvent)));
        console.info('Sistema inicializado com sucesso');
    }

    // Registrar evento de autenticação
    async registerAuthEvent(ctx, userId, userEmail, eventType, decision, riskScore, ipAddress, location, dataHash, timestamp) {
        console.info(`Registrando evento de autenticação para usuário: ${userEmail}`);

        const authEvent = {
            userId: userId,
            userEmail: userEmail,
            eventType: eventType,
            decision: decision,
            riskScore: parseFloat(riskScore),
            ipAddress: ipAddress,
            location: location,
            dataHash: dataHash,
            timestamp: timestamp,
            confirmations: 0,
            blockNumber: ctx.stub.getTxID(),
            txId: ctx.stub.getTxID()
        };

        // Usar o txId como chave
        const key = ctx.stub.getTxID();
        await ctx.stub.putState(key, Buffer.from(JSON.stringify(authEvent)));

        // Emitir evento para notificação
        ctx.stub.setEvent('AuthEventRegistered', Buffer.from(JSON.stringify({
            txId: key,
            userId: userId,
            eventType: eventType,
            timestamp: timestamp
        })));

        console.info(`Evento registrado com sucesso - TxID: ${key}`);
        return key;
    }

    // Consultar evento específico
    async queryAuthEvent(ctx, txId) {
        console.info(`Consultando evento: ${txId}`);

        const eventBytes = await ctx.stub.getState(txId);
        if (!eventBytes || eventBytes.length === 0) {
            throw new Error(`Evento ${txId} não encontrado`);
        }

        return eventBytes.toString();
    }

    // Consultar histórico de um usuário
    async queryUserHistory(ctx, userId) {
        console.info(`Consultando histórico do usuário: ${userId}`);

        const query = {
            selector: {
                userId: userId
            },
            sort: [
                { timestamp: 'desc' }
            ]
        };

        const iterator = await ctx.stub.getQueryResult(JSON.stringify(query));
        const results = await this._getAllResults(iterator);

        return JSON.stringify(results);
    }

    // Consultar transações por período
    async queryTransactionsByPeriod(ctx, startTime, endTime) {
        console.info(`Consultando transações entre ${startTime} e ${endTime}`);

        const query = {
            selector: {
                timestamp: {
                    '$gte': startTime,
                    '$lte': endTime
                }
            },
            sort: [
                { timestamp: 'desc' }
            ]
        };

        const iterator = await ctx.stub.getQueryResult(JSON.stringify(query));
        const results = await this._getAllResults(iterator);

        return JSON.stringify(results);
    }

    // Consultar transações de alto risco
    async queryHighRiskTransactions(ctx, minRiskScore) {
        console.info(`Consultando transações de alto risco (>${minRiskScore})`);

        const query = {
            selector: {
                riskScore: {
                    '$gte': parseFloat(minRiskScore)
                }
            },
            sort: [
                { riskScore: 'desc' },
                { timestamp: 'desc' }
            ]
        };

        const iterator = await ctx.stub.getQueryResult(JSON.stringify(query));
        const results = await this._getAllResults(iterator);

        return JSON.stringify(results);
    }

    // Verificar se uma transação existe
    async verifyTransaction(ctx, txId) {
        console.info(`Verificando transação: ${txId}`);

        const eventBytes = await ctx.stub.getState(txId);
        return (eventBytes && eventBytes.length > 0).toString();
    }

    // Registrar evento personalizado
    async registerCustomEvent(ctx, eventType, eventData, userId, timestamp) {
        console.info(`Registrando evento personalizado: ${eventType}`);

        const customEvent = {
            eventType: eventType,
            eventData: eventData,
            userId: userId,
            timestamp: timestamp,
            txId: ctx.stub.getTxID()
        };

        const key = ctx.stub.getTxID();
        await ctx.stub.putState(key, Buffer.from(JSON.stringify(customEvent)));

        // Emitir evento
        ctx.stub.setEvent('CustomEventRegistered', Buffer.from(JSON.stringify({
            txId: key,
            eventType: eventType,
            userId: userId,
            timestamp: timestamp
        })));

        return key;
    }

    // Ping para teste de conectividade
    async ping(ctx) {
        console.info('Ping recebido');
        return 'pong';
    }

    // Obter informações do chaincode
    async getInfo(ctx) {
        return JSON.stringify({
            name: 'AuthAuditContract',
            version: '1.0.0',
            description: 'Chaincode para auditoria de eventos de autenticação',
            timestamp: new Date().toISOString()
        });
    }

    // Método auxiliar para processar resultados de consulta
    async _getAllResults(iterator) {
        const allResults = [];
        let res = await iterator.next();

        while (!res.done) {
            if (res.value && res.value.value.toString()) {
                const jsonRes = {};
                jsonRes.Key = res.value.key;
                
                try {
                    jsonRes.Record = JSON.parse(res.value.value.toString('utf8'));
                } catch (err) {
                    console.log(err);
                    jsonRes.Record = res.value.value.toString('utf8');
                }
                
                allResults.push(jsonRes);
            }
            res = await iterator.next();
        }

        iterator.close();
        return allResults;
    }

    // Método para consultar com paginação
    async queryWithPagination(ctx, queryString, pageSize, bookmark) {
        const { iterator, metadata } = await ctx.stub.getQueryResultWithPagination(
            queryString, 
            parseInt(pageSize), 
            bookmark
        );

        const results = await this._getAllResults(iterator);

        return JSON.stringify({
            results: results,
            metadata: {
                recordsCount: metadata.fetchedRecordsCount,
                bookmark: metadata.bookmark
            }
        });
    }

    // Obter estatísticas gerais
    async getStatistics(ctx) {
        console.info('Obtendo estatísticas gerais');

        // Consultar todos os eventos (limitado)
        const query = {
            selector: {},
            fields: ['eventType', 'decision', 'riskScore', 'timestamp']
        };

        const iterator = await ctx.stub.getQueryResult(JSON.stringify(query));
        const results = await this._getAllResults(iterator);

        // Calcular estatísticas
        const stats = {
            totalEvents: results.length,
            eventTypes: {},
            decisions: {},
            avgRiskScore: 0,
            timestamp: new Date().toISOString()
        };

        let totalRiskScore = 0;
        let validRiskScores = 0;

        results.forEach(result => {
            const record = result.Record;
            
            // Contar tipos de evento
            stats.eventTypes[record.eventType] = (stats.eventTypes[record.eventType] || 0) + 1;
            
            // Contar decisões
            stats.decisions[record.decision] = (stats.decisions[record.decision] || 0) + 1;
            
            // Calcular média de risk score
            if (record.riskScore && !isNaN(record.riskScore)) {
                totalRiskScore += parseFloat(record.riskScore);
                validRiskScores++;
            }
        });

        if (validRiskScores > 0) {
            stats.avgRiskScore = totalRiskScore / validRiskScores;
        }

        return JSON.stringify(stats);
    }
}

module.exports = AuthAuditContract; 