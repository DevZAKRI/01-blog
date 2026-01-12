import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Report, CreateReportRequest } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  constructor(private http: HttpClient) {}

  /**
   * Create a report against a user
   */
  createReport(data: CreateReportRequest): Observable<Report> {
    return this.http.post<Report>(`${environment.apiUrl}/users/${data.reportedUserId}/report`, { reason: data.reason });
  }
}
