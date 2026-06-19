# -*- coding: utf-8 -*-
import numpy as np, time, json
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import IsolationForest, RandomForestClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.model_selection import train_test_split, StratifiedKFold
from sklearn.metrics import (accuracy_score, precision_score, recall_score,
                             f1_score, roc_auc_score, confusion_matrix)

rng=np.random.default_rng(42)
N=50000; FR=0.05; NS=int(N*FR); NL=N-NS

def gen_legit(m):
    hora=np.clip(rng.normal(13,3.2,m),0,23)
    dia=np.clip(rng.integers(0,5,m)+rng.normal(0,0.4,m),0,6)
    interval=rng.exponential(8,m)
    freq=rng.poisson(5,m).astype(float)
    dist=np.abs(rng.normal(0,4,m))
    rede=rng.choice([0,1,2],m,p=[0.6,0.35,0.05]).astype(float)
    asn=(rng.random(m)>0.03).astype(float)
    vel=np.abs(rng.normal(8,8,m))
    fp=np.clip(rng.normal(0.92,0.06,m),0,1)
    so=(rng.random(m)>0.03).astype(float)
    disp=(rng.random(m)>0.05).astype(float)
    auto=(rng.random(m)<0.02).astype(float)
    dig=np.clip(rng.normal(0.89,0.07,m),0,1)
    falhas=rng.poisson(0.3,m).astype(float)
    return np.column_stack([hora,dia,interval,freq,dist,rede,asn,vel,fp,so,disp,auto,dig,falhas])

def corrupt(X,kmin=1,kmax=4):
    m=X.shape[0]
    for i in range(m):
        k=rng.integers(kmin,kmax)
        for j in rng.choice(14,k,replace=False):
            if   j==0: X[i,0]=rng.choice([rng.uniform(2,6),rng.uniform(21,23.9)])
            elif j==1: X[i,1]=rng.choice([5,6])
            elif j==2: X[i,2]=rng.exponential(0.4)
            elif j==3: X[i,3]=rng.poisson(18)+5
            elif j==4: X[i,4]=rng.uniform(300,4000)
            elif j==5: X[i,5]=rng.choice([2,3])
            elif j==6: X[i,6]=0
            elif j==7: X[i,7]=rng.uniform(200,1200)
            elif j==8: X[i,8]=rng.uniform(0.35,0.72)
            elif j==9: X[i,9]=0
            elif j==10: X[i,10]=0
            elif j==11: X[i,11]=1
            elif j==12: X[i,12]=rng.uniform(0.35,0.72)
            elif j==13: X[i,13]=rng.poisson(6)+3
    return X

Xl=gen_legit(NL)
# ~5% dos legitimos exibem uma anomalia branda (gera falsos positivos realistas)
amask=rng.random(NL)<0.05
Xl[amask]=corrupt(Xl[amask],1,2)
Xs=corrupt(gen_legit(NS),2,5)
X=np.vstack([Xl,Xs]); y=np.concatenate([np.zeros(NL),np.ones(NS)]).astype(int)
# ruido para sobreposicao realista
X=X+rng.normal(0,0.30,X.shape)*X.std(0)
idx=rng.permutation(N); X,y=X[idx],y[idx]

Xtr,Xte,ytr,yte=train_test_split(X,y,test_size=0.30,stratify=y,random_state=42)
sc=StandardScaler().fit(Xtr); Xtr_s=sc.transform(Xtr); Xte_s=sc.transform(Xte)

# modelos
iso=IsolationForest(n_estimators=100,contamination=FR,random_state=42).fit(Xtr_s)
def iso_score(Xs):
    s=-iso.score_samples(Xs); return s
s_tr=iso_score(Xtr_s); lo,hi=s_tr.min(),s_tr.max()
def iso_norm(Xs): return np.clip((iso_score(Xs)-lo)/(hi-lo),0,1)

rf=RandomForestClassifier(n_estimators=120,max_depth=None,n_jobs=-1,random_state=42,class_weight="balanced").fit(Xtr_s,ytr)
mlp=MLPClassifier(hidden_layer_sizes=(64,32),activation="relu",max_iter=220,random_state=42).fit(Xtr_s,ytr)

def ens_score(Xs):
    return 0.4*iso_norm(Xs)+0.3*rf.predict_proba(Xs)[:,1]+0.3*mlp.predict_proba(Xs)[:,1]

sc_tr=ens_score(Xtr_s); sc_te=ens_score(Xte_s)
# threshold por melhor F1 no treino
ths=np.linspace(0.2,0.8,61); best=max(ths,key=lambda t:f1_score(ytr,(sc_tr>t).astype(int)))
pred=(sc_te>best).astype(int)
tn,fp,fn,tp=confusion_matrix(yte,pred).ravel()
M=dict(
 acc=accuracy_score(yte,pred), prec=precision_score(yte,pred), rec=recall_score(yte,pred),
 f1=f1_score(yte,pred), auc=roc_auc_score(yte,sc_te), fpr=fp/(fp+tn),
 thr=float(best), tn=int(tn),fp=int(fp),fn=int(fn),tp=int(tp))

# 5-fold CV (acuracia/F1 do ensemble)
skf=StratifiedKFold(5,shuffle=True,random_state=42); accs=[];f1s=[]
for tri,tei in skf.split(X,y):
    s2=StandardScaler().fit(X[tri]); a=s2.transform(X[tri]); b=s2.transform(X[tei])
    iso2=IsolationForest(n_estimators=100,contamination=FR,random_state=42).fit(a)
    st=-iso2.score_samples(a); l2,h2=st.min(),st.max()
    rf2=RandomForestClassifier(n_estimators=120,n_jobs=-1,random_state=42,class_weight="balanced").fit(a,y[tri])
    mlp2=MLPClassifier(hidden_layer_sizes=(64,32),max_iter=180,random_state=42).fit(a,y[tri])
    sc2=0.4*np.clip((-iso2.score_samples(b)-l2)/(h2-l2),0,1)+0.3*rf2.predict_proba(b)[:,1]+0.3*mlp2.predict_proba(b)[:,1]
    p2=(sc2>best).astype(int)
    accs.append(accuracy_score(y[tei],p2)); f1s.append(f1_score(y[tei],p2))
M["cv_acc_mean"]=float(np.mean(accs)); M["cv_acc_std"]=float(np.std(accs))
M["cv_f1_mean"]=float(np.mean(f1s)); M["cv_f1_std"]=float(np.std(f1s))

# latencia por REQUISICAO UNICA (1 amostra por vez), mediana de 300 chamadas
def lat1(fn,Xs,reps=300):
    ts=[]
    for i in range(reps):
        x=Xs[i:i+1]
        t0=time.perf_counter(); fn(x); ts.append((time.perf_counter()-t0)*1000.0)
    return float(np.median(ts))
M["lat_if"]=lat1(lambda x: iso_norm(x),Xte_s)
M["lat_rf"]=lat1(lambda x: rf.predict_proba(x),Xte_s)
M["lat_dl"]=lat1(lambda x: mlp.predict_proba(x),Xte_s)
M["lat_ens"]=lat1(lambda x: ens_score(x),Xte_s)

json.dump(M,open(r"C:/Users/G4F/AppData/Local/Temp/metrics.json","w"),indent=2)
print("=== RESULTADOS (conjunto de teste, 30%) ===")
for k in ["acc","prec","rec","f1","auc","fpr"]:
    print("  %-5s = %.4f (%.1f%%)"%(k,M[k],M[k]*100))
print("  threshold = %.3f | TN=%d FP=%d FN=%d TP=%d"%(M["thr"],tn,fp,fn,tp))
print("  CV5 acc = %.3f±%.3f | F1 = %.3f±%.3f"%(M["cv_acc_mean"],M["cv_acc_std"],M["cv_f1_mean"],M["cv_f1_std"]))
print("  latencia ms/amostra: IF=%.4f RF=%.4f DL=%.4f Ensemble=%.4f"%(M["lat_if"],M["lat_rf"],M["lat_dl"],M["lat_ens"]))
