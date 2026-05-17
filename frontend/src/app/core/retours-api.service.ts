import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AuthUserResponse,
  CreateNonConformiteRequest,
  CreateRetourRequest,
  CreateUtilisateurRequest,
  EtatTraitement,
  HistoriqueRetour,
  LoginRequest,
  LoginResponse,
  NonConformite,
  Retour,
  UpdateEtatRetourRequest,
  Utilisateur,
} from './retours.models';

@Injectable({ providedIn: 'root' })
export class RetoursApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, payload);
  }

  me(): Observable<AuthUserResponse> {
    return this.http.get<AuthUserResponse>(`${this.baseUrl}/auth/me`);
  }

  listRetours(etat?: EtatTraitement): Observable<Retour[]> {
    let params = new HttpParams();
    if (etat) {
      params = params.set('etat', etat);
    }
    return this.http.get<Retour[]>(`${this.baseUrl}/retours`, { params });
  }

  createRetour(payload: CreateRetourRequest): Observable<Retour> {
    return this.http.post<Retour>(`${this.baseUrl}/retours`, payload);
  }

  updateEtatRetour(id: number, payload: UpdateEtatRetourRequest): Observable<Retour> {
    return this.http.put<Retour>(`${this.baseUrl}/retours/${id}/etat`, payload);
  }

  deleteRetour(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/retours/${id}`);
  }

  listNonConformites(produit?: string): Observable<NonConformite[]> {
    let params = new HttpParams();
    if (produit) {
      params = params.set('produit', produit);
    }
    return this.http.get<NonConformite[]>(`${this.baseUrl}/non-conformites`, { params });
  }

  createNonConformite(payload: CreateNonConformiteRequest): Observable<NonConformite> {
    return this.http.post<NonConformite>(`${this.baseUrl}/non-conformites`, payload);
  }

  deleteNonConformite(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/non-conformites/${id}`);
  }

  listUtilisateurs(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(`${this.baseUrl}/utilisateurs`);
  }

  createUtilisateur(payload: CreateUtilisateurRequest): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(`${this.baseUrl}/utilisateurs`, payload);
  }

  deleteUtilisateur(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/utilisateurs/${id}`);
  }

  listHistoriques(retourId?: number): Observable<HistoriqueRetour[]> {
    if (!retourId) {
      return this.http.get<HistoriqueRetour[]>(`${this.baseUrl}/historiques`);
    }
    return this.http.get<HistoriqueRetour[]>(`${this.baseUrl}/historiques/retour/${retourId}`);
  }
}
