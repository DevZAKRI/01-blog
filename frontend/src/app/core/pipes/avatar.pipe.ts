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
    // If the URL is already the full API URL or an absolute URL, return it as-is
    if (url.startsWith(environment.apiUrl) || /^https?:\/\//i.test(url)) return url;

    // Compute API root without the /api/v1 prefix so uploads are served from /uploads/**
    const apiRoot = environment.apiUrl.replace(/\/api\/v1\/?$/, '');

    // If it's a server-relative uploads path, prefix with API root
    if (url.startsWith('/uploads')) return `${apiRoot}${url}`;

    // Otherwise treat it as a relative path and prefix with API root
    const normalized = url.startsWith('/') ? url : `/${url}`;
    return `${apiRoot}${normalized}`;
  }

  private robo(username: string, size: string) {
    const u = encodeURIComponent(String(username || 'unknown'));
    return `https://robohash.org/${u}.png?size=${size}`;
  }
}
