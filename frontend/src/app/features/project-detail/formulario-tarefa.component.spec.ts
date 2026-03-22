import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { FormularioTarefaComponent, NovaTarefa } from './formulario-tarefa.component';
import { RespostaMembro } from '../../core/models/project.model';

describe('FormularioTarefaComponent', () => {
  let component: FormularioTarefaComponent;
  let fixture: ComponentFixture<FormularioTarefaComponent>;

  const membrosMock: RespostaMembro[] = [
    { idUsuario: 1, nomeUsuario: 'Alice', emailUsuario: 'alice@test.com', perfil: 'ADMIN', entradoEm: '2026-01-01T00:00:00' },
    { idUsuario: 2, nomeUsuario: 'Bob', emailUsuario: 'bob@test.com', perfil: 'MEMBER', entradoEm: '2026-01-01T00:00:00' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioTarefaComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioTarefaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve ter prioridade MEDIUM como padrão', () => {
    expect(component.prioridade).toBe('MEDIUM');
  });

  it('não deve emitir evento se título estiver vazio', () => {
    spyOn(component.criar, 'emit');
    component.titulo = '   ';
    component.submeter();
    expect(component.criar.emit).not.toHaveBeenCalled();
  });

  it('deve emitir evento com dados mínimos ao submeter título válido', () => {
    spyOn(component.criar, 'emit');
    component.titulo = 'Minha tarefa';
    component.submeter();

    expect(component.criar.emit).toHaveBeenCalledWith(
      jasmine.objectContaining({ titulo: 'Minha tarefa', prioridade: 'MEDIUM' })
    );
  });

  it('deve incluir descrição quando preenchida', () => {
    spyOn(component.criar, 'emit');
    component.titulo = 'Tarefa';
    component.descricao = 'Detalhes da tarefa';
    component.submeter();

    const emitido = (component.criar.emit as jasmine.Spy).calls.mostRecent().args[0] as NovaTarefa;
    expect(emitido.descricao).toBe('Detalhes da tarefa');
  });

  it('não deve incluir descrição vazia no evento', () => {
    spyOn(component.criar, 'emit');
    component.titulo = 'Tarefa';
    component.descricao = '   ';
    component.submeter();

    const emitido = (component.criar.emit as jasmine.Spy).calls.mostRecent().args[0] as NovaTarefa;
    expect(emitido.descricao).toBeUndefined();
  });

  it('deve incluir responsável quando selecionado', () => {
    spyOn(component.criar, 'emit');
    component.titulo = 'Tarefa';
    component.idResponsavel = 2;
    component.submeter();

    const emitido = (component.criar.emit as jasmine.Spy).calls.mostRecent().args[0] as NovaTarefa;
    expect(emitido.idResponsavel).toBe(2);
  });

  it('deve formatar prazo com horário ao submeter', () => {
    spyOn(component.criar, 'emit');
    component.titulo = 'Tarefa';
    component.prazo = '2026-04-15';
    component.submeter();

    const emitido = (component.criar.emit as jasmine.Spy).calls.mostRecent().args[0] as NovaTarefa;
    expect(emitido.prazo).toBe('2026-04-15T23:59:59');
  });

  it('deve resetar campos após submissão', () => {
    component.titulo = 'Tarefa';
    component.descricao = 'Detalhe';
    component.idResponsavel = 1;
    component.prazo = '2026-04-15';
    component.submeter();

    expect(component.titulo).toBe('');
    expect(component.descricao).toBe('');
    expect(component.idResponsavel).toBeNull();
    expect(component.prazo).toBe('');
  });

  it('deve respeitar a prioridade selecionada', () => {
    spyOn(component.criar, 'emit');
    component.titulo = 'Tarefa urgente';
    component.prioridade = 'CRITICAL';
    component.submeter();

    const emitido = (component.criar.emit as jasmine.Spy).calls.mostRecent().args[0] as NovaTarefa;
    expect(emitido.prioridade).toBe('CRITICAL');
  });

  it('deve renderizar opções de membros no template', () => {
    component.membros = membrosMock;
    fixture.detectChanges();

    const options = fixture.nativeElement.querySelectorAll('select option');
    const nomes = Array.from(options).map((o: any) => o.textContent.trim());
    expect(nomes).toContain('Alice');
    expect(nomes).toContain('Bob');
  });

  it('botão Criar deve estar desabilitado com título vazio', () => {
    component.titulo = '';
    fixture.detectChanges();

    const btn = fixture.nativeElement.querySelector('button');
    expect(btn.disabled).toBeTrue();
  });

  it('botão Criar deve estar habilitado com título preenchido', async () => {
    component.titulo = 'Algo';
    fixture.detectChanges();
    await fixture.whenStable();

    const btn = fixture.nativeElement.querySelector('button');
    expect(btn.disabled).toBeFalse();
  });
});
