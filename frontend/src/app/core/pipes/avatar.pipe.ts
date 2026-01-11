import { Pipe, PipeTransform } from '@angular/core';
import { environment } from '../../../environments/environment';

@Pipe({ name: 'avatar', standalone: true })
export class AvatarPipe implements PipeTransform {
  transform(value: any, size: string = '120x120'): string {
    if (!value) return this.robo('unknown', size);

    // if value is a string it's probably already an URL
    if (typeof value === 'string') {
      return this.resolve(value);
    }

    // If value is an object with avatar/avatarUrl/username
    const avatar = value.avatar ?? value.avatarUrl;
    const username = value.username ?? value.name ?? 'unknown';

    if (avatar) return this.resolve(avatar);

    return this.robo(username, size);
  }

  private resolve(url: string): string {
    if (!url) return this.robo('unknown', '120x120');
    // If the URL is already a full URL, return it as-is
    if (url.startsWith(environment.apiUrl)) return url;
    // If it's a server-relative uploads path, prefix with API URL
    if (url.startsWith('/uploads')) return `${environment.apiUrl}${url}`;
    // If it's an absolute http/https URL, return as-is
    if (/^https?:\/\//i.test(url)) return url;
    // Otherwise treat it as a relative path and prefix with API URL
    const normalized = url.startsWith('/') ? url : `/${url}`;
    return `${environment.apiUrl}${normalized}`;
  }

  private robo(username: string, size: string) {
    const u = encodeURIComponent(String(username || 'unknown'));
    return `https://robohash.org/${u}.png?size=${size}`;
  }
}
