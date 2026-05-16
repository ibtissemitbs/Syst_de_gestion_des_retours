export type EtatTraitement = 'ENREGISTRE' | 'EN_COURS' | 'VALIDE' | 'REJETE';
export type Gravite = 'FAIBLE' | 'MOYENNE' | 'ELEVEE' | 'CRITIQUE';
export type Role = 'ADMIN' | 'AGENT' | 'QUALITE';

export interface Retour {
  id: number;
  produit: string;
  client: string;
  raison: string;
  etatTraitement: EtatTraitement;
  date: string;
  stockMisAJour: boolean;
}

export interface NonConformite {
  id: number;
  description: string;
  gravite: Gravite;
  produit: string;
  retourProduitId: number | null;
  date: string;
}

export interface Utilisateur {
  id: number;
  nom: string;
  email: string;
  enabled?: boolean;
  role: Role;
}

export interface HistoriqueRetour {
  id: number;
  retourProduitId: number;
  retourProduit?: string | null;
  retourClient?: string | null;
  retourRaison?: string | null;
  action: string;
  employeId?: number | null;
  employeNom?: string | null;
  employeEmail?: string | null;
  employeRole?: Role | null;
  date: string;
}

export interface CreateRetourRequest {
  produit: string;
  client: string;
  raison: string;
}

export interface UpdateEtatRetourRequest {
  etatTraitement: EtatTraitement;
  employeId?: number;
}

export interface CreateNonConformiteRequest {
  description: string;
  gravite: Gravite;
  produit: string;
  retourId?: number;
}

export interface CreateUtilisateurRequest {
  nom: string;
  email: string;
  password: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthUserResponse {
  id: number;
  nom: string;
  email: string;
  role: Role;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUserResponse;
}
