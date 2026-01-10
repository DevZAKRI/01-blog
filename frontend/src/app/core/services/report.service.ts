import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Report, CreateReportRequest } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  constructor(private http: HttpClient) {}

  createReport(data: CreateReportRequest): Observable<Report> {
    // backend expects POST /users/{id}/report with { reason }
    return this.http.post<Report>(`${environment.apiUrl}/users/${data.reportedUserId}/report`, { reason: data.reason });
  }

  getReports(): Observable<Report[]> {
    return this.http.get<any>(`${environment.apiUrl}/admin/reports`).pipe(map((page) => page?.content || page));
  }

  updateReportStatus(reportId: string, status: string): Observable<Report> {
    return this.http.patch<Report>(`${environment.apiUrl}/admin/reports/${reportId}`, { status });
  }
}
