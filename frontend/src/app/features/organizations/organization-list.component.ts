import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { SdwanApiService } from '../../core/sdwan-api.service';

@Component({
  selector: 'app-organization-list',
  standalone: true,
  templateUrl: './organization-list.component.html',
  styleUrl: './organization-list.component.scss',
})
export class OrganizationListComponent implements OnInit {
  private readonly api    = inject(SdwanApiService);
  private readonly router = inject(Router);

  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getOrganizations().subscribe({
      next: orgs => {
        if (orgs.length > 0) {
          this.router.navigate(['/organizations', orgs[0].id], { replaceUrl: true });
        } else {
          this.error.set('No organizations found.');
        }
      },
      error: () => this.error.set('Failed to load organizations.'),
    });
  }
}
