import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { HealthResponse } from './models';

@Injectable({ providedIn: 'root' })
export class SdwanApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';

  getHealth() {
    return this.http.get<HealthResponse>(`${this.baseUrl}/health`);
  }
}
