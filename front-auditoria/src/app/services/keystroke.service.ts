import { Injectable } from '@angular/core';

export interface KeystrokeEvent {
  tecla: string;
  keyDown: number;
  keyUp: number;
}

@Injectable({
  providedIn: 'root'
})
export class KeystrokeService {
  private eventos: KeystrokeEvent[] = [];
  private capturando = false;

  iniciarCaptura(): void {
    this.eventos = [];
    this.capturando = true;
  }

  registrarKeyDown(event: KeyboardEvent): void {
    if (!this.capturando) return;
    this.eventos.push({
      tecla: event.key,
      keyDown: Date.now(),
      keyUp: 0
    });
  }

  registrarKeyUp(event: KeyboardEvent): void {
    if (!this.capturando) return;
    const evento = this.eventos
      .filter(e => e.tecla === event.key && e.keyUp === 0)
      .pop();
    if (evento) {
      evento.keyUp = Date.now();
    }
  }

  finalizarCaptura(): KeystrokeEvent[] {
    this.capturando = false;
    return this.eventos.filter(e => e.keyUp > 0);
  }

  obterDados(): KeystrokeEvent[] {
    return this.eventos.filter(e => e.keyUp > 0);
  }
}
