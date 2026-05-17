import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AuthUserResponse,
  CreateNonConformiteRequest,
  CreateRetourRequest,
  CreateUtilisateurRequest,
  EtatTraitement,
  Gravite,
  HistoriqueRetour,
  NonConformite,
  Retour,
  Role,
  Utilisateur,
} from './core/retours.models';
import { RetoursApiService } from './core/retours-api.service';

const TOKEN_KEY = 'retours_access_token';
type Tab = 'retours' | 'non-conformites' | 'historique' | 'utilisateurs';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly api = inject(RetoursApiService);

  readonly etats: EtatTraitement[] = ['ENREGISTRE', 'EN_COURS', 'VALIDE', 'REJETE'];
  readonly gravites: Gravite[] = ['FAIBLE', 'MOYENNE', 'ELEVEE', 'CRITIQUE'];
  readonly roles: Role[] = ['AGENT', 'QUALITE', 'ADMIN'];

  readonly activeTab = signal<Tab>('retours');
  readonly user = signal<AuthUserResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly notice = signal('');

  readonly retours = signal<Retour[]>([]);
  readonly nonConformites = signal<NonConformite[]>([]);
  readonly historiques = signal<HistoriqueRetour[]>([]);
  readonly utilisateurs = signal<Utilisateur[]>([]);
  readonly filtreEtat = signal<EtatTraitement | ''>('');

  loginForm = {
    email: '',
    password: '',
  };

  retourForm: CreateRetourRequest = {
    produit: '',
    client: '',
    raison: '',
  };

  nonConformiteForm: CreateNonConformiteRequest = {
    description: '',
    gravite: 'MOYENNE',
    produit: '',
    retourId: undefined,
  };

  utilisateurForm: CreateUtilisateurRequest = {
    nom: '',
    email: '',
    password: '',
    role: 'AGENT',
  };

  readonly totalRetours = computed(() => this.retours().length);
  readonly retoursEnCours = computed(() => this.retours().filter((retour) => retour.etatTraitement === 'EN_COURS').length);
  readonly retoursValides = computed(() => this.retours().filter((retour) => retour.etatTraitement === 'VALIDE').length);
  readonly nonConformitesCritiques = computed(() =>
    this.nonConformites().filter((item) => item.gravite === 'CRITIQUE').length,
  );
  readonly canCreateRetours = computed(() => ['AGENT', 'QUALITE'].includes(this.user()?.role ?? ''));
  readonly canViewNonConformites = computed(() => ['AGENT', 'QUALITE'].includes(this.user()?.role ?? ''));
  readonly canManageQuality = computed(() => this.user()?.role === 'QUALITE');
  readonly canManageUsers = computed(() => this.user()?.role === 'ADMIN');

  ngOnInit(): void {
    if (localStorage.getItem(TOKEN_KEY)) {
      this.refreshCurrentUser();
    }
  }

  login(): void {
    this.clearMessages();
    this.loading.set(true);
    localStorage.removeItem(TOKEN_KEY);

    this.api
      .login(this.loginForm)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          localStorage.setItem(TOKEN_KEY, response.accessToken);
          this.user.set(response.user);
          this.notice.set(`Bienvenue ${response.user.nom}`);
          this.loadWorkspace();
        },
        error: (error) => this.error.set(this.getErrorMessage(error, 'Connexion impossible.')),
      });
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.user.set(null);
    this.retours.set([]);
    this.nonConformites.set([]);
    this.historiques.set([]);
    this.utilisateurs.set([]);
    this.clearMessages();
  }

  setTab(tab: Tab): void {
    if (tab === 'non-conformites' && !this.canViewNonConformites()) {
      this.activeTab.set('retours');
      return;
    }

    if (tab === 'utilisateurs' && !this.canManageUsers()) {
      this.activeTab.set('retours');
      return;
    }

    this.activeTab.set(tab);
    if (tab === 'utilisateurs' && this.canManageUsers()) {
      this.loadUtilisateurs();
    }
  }

  setFiltreEtat(value: string): void {
    this.filtreEtat.set(value as EtatTraitement | '');
    this.loadRetours();
  }

  loadWorkspace(): void {
    this.loadRetours();
    this.loadHistoriques();
    if (this.canViewNonConformites()) {
      this.loadNonConformites();
    } else {
      this.nonConformites.set([]);
    }
    if (this.canManageUsers()) {
      this.loadUtilisateurs();
    }
  }

  createRetour(): void {
    if (!this.canCreateRetours()) {
      this.error.set('Seuls les agents et le service qualite peuvent enregistrer un retour.');
      return;
    }

    this.clearMessages();
    this.api.createRetour(this.retourForm).subscribe({
      next: (retour) => {
        this.retours.update((retours) => [retour, ...retours]);
        this.retourForm = { produit: '', client: '', raison: '' };
        this.notice.set('Retour enregistre.');
        this.loadHistoriques();
      },
      error: (error) => this.error.set(this.getErrorMessage(error, 'Retour non enregistre.')),
    });
  }

  updateEtat(retour: Retour, etatTraitement: EtatTraitement): void {
    this.clearMessages();
    this.api.updateEtatRetour(retour.id, { etatTraitement }).subscribe({
      next: (updated) => {
        this.retours.update((retours) => retours.map((item) => (item.id === updated.id ? updated : item)));
        this.notice.set('Etat mis a jour.');
        this.loadHistoriques(retour.id);
      },
      error: (error) => this.error.set(this.getErrorMessage(error, 'Etat non modifie.')),
    });
  }

  deleteRetour(id: number): void {
    this.clearMessages();
    this.api.deleteRetour(id).subscribe({
      next: () => {
        this.retours.update((retours) => retours.filter((retour) => retour.id !== id));
        this.notice.set('Retour supprime.');
      },
      error: (error) => this.error.set(this.getErrorMessage(error, 'Suppression impossible.')),
    });
  }

  createNonConformite(): void {
    if (!this.canManageQuality()) {
      this.error.set('La non-conformite est reservee au service qualite.');
      return;
    }

    this.clearMessages();
    const payload = {
      ...this.nonConformiteForm,
      retourId: this.nonConformiteForm.retourId || undefined,
    };

    this.api.createNonConformite(payload).subscribe({
      next: (nonConformite) => {
        this.nonConformites.update((items) => [nonConformite, ...items]);
        this.nonConformiteForm = { description: '', gravite: 'MOYENNE', produit: '', retourId: undefined };
        this.notice.set('Non-conformite enregistree.');
      },
      error: (error) => this.error.set(this.getErrorMessage(error, 'Non-conformite non enregistree.')),
    });
  }

  deleteNonConformite(id: number): void {
    this.clearMessages();
    this.api.deleteNonConformite(id).subscribe({
      next: () => {
        this.nonConformites.update((items) => items.filter((item) => item.id !== id));
        this.notice.set('Non-conformite supprimee.');
      },
      error: (error) => this.error.set(this.getErrorMessage(error, 'Suppression impossible.')),
    });
  }

  createUtilisateur(): void {
    this.clearMessages();
    this.api.createUtilisateur(this.utilisateurForm).subscribe({
      next: (utilisateur) => {
        this.utilisateurs.update((items) => [utilisateur, ...items]);
        this.utilisateurForm = { nom: '', email: '', password: '', role: 'AGENT' };
        this.notice.set('Utilisateur cree.');
      },
      error: (error) => this.error.set(this.getErrorMessage(error, 'Utilisateur non cree.')),
    });
  }

  deleteUtilisateur(id: number): void {
    this.clearMessages();
    this.api.deleteUtilisateur(id).subscribe({
      next: () => {
        this.utilisateurs.update((items) => items.filter((item) => item.id !== id));
        this.notice.set('Utilisateur supprime.');
      },
      error: (error) => this.error.set(this.getErrorMessage(error, 'Suppression impossible.')),
    });
  }

  formatDate(value: string): string {
    if (!value) {
      return '-';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  historiqueRetourLabel(item: HistoriqueRetour): string {
    const produit = item.retourProduit ?? `Retour #${item.retourProduitId}`;
    const client = item.retourClient ? ` - ${item.retourClient}` : '';
    return `${produit}${client}`;
  }

  historiqueUserLabel(item: HistoriqueRetour): string {
    if (item.employeNom && item.employeRole) {
      return `${item.employeNom} (${item.employeRole})`;
    }

    if (item.employeNom) {
      return item.employeNom;
    }

    if (item.employeEmail) {
      return item.employeEmail;
    }

    return 'Utilisateur non renseigne';
  }

  labelEtat(etat: EtatTraitement): string {
    return {
      ENREGISTRE: 'Enregistre',
      EN_COURS: 'En cours',
      VALIDE: 'Valide',
      REJETE: 'Rejete',
    }[etat];
  }

  private refreshCurrentUser(): void {
    this.loading.set(true);
    this.api
      .me()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (user) => {
          this.user.set(user);
          this.loadWorkspace();
        },
        error: () => this.logout(),
      });
  }

  private loadRetours(): void {
    this.api.listRetours(this.filtreEtat() || undefined).subscribe({
      next: (retours) => this.retours.set(retours),
      error: (error) => this.error.set(this.getErrorMessage(error, 'Retours indisponibles.')),
    });
  }

  private loadNonConformites(): void {
    this.api.listNonConformites().subscribe({
      next: (items) => this.nonConformites.set(items),
      error: (error) => this.error.set(this.getErrorMessage(error, 'Non-conformites indisponibles.')),
    });
  }

  private loadHistoriques(retourId?: number): void {
    this.api.listHistoriques(retourId).subscribe({
      next: (items) => this.historiques.set(items),
      error: (error) => this.error.set(this.getErrorMessage(error, 'Historique indisponible.')),
    });
  }

  private loadUtilisateurs(): void {
    this.api.listUtilisateurs().subscribe({
      next: (utilisateurs) => this.utilisateurs.set(utilisateurs),
      error: (error) => this.error.set(this.getErrorMessage(error, 'Utilisateurs indisponibles.')),
    });
  }

  private clearMessages(): void {
    this.error.set('');
    this.notice.set('');
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const response = (error as { error?: { message?: string; error?: string } }).error;
      return response?.message ?? response?.error ?? fallback;
    }

    return fallback;
  }
}
