import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { catchError, map, of, startWith } from 'rxjs';
import { SdwanApiService } from './core/sdwan-api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, AsyncPipe],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  private readonly api = inject(SdwanApiService);

  protected readonly today = new Intl.DateTimeFormat('en', {
    dateStyle: 'medium'
  }).format(new Date());

  protected readonly apiStatus$ = this.api.getHealth().pipe(
    map((response) => ({
      label: 'Backend reachable',
      detail: `${response.service} responded at ${response.timestamp}.`,
      className: 'status-card--ok'
    })),
    startWith({
      label: 'Checking backend',
      detail: 'Waiting for http://localhost:8080/api/health',
      className: 'status-card--pending'
    }),
    catchError(() =>
      of({
        label: 'Backend unavailable',
        detail: 'Start boilerplate/backend when you begin the API portion of the assignment.',
        className: 'status-card--error'
      })
    )
  );

  protected readonly buildItems = [
    'Dashboard overview with site and device summaries',
    'Organization -> Site -> Device navigation flow',
    'Backend contracts for overview, site detail, and device detail',
    'Site health aggregation from device availability',
    'WAN-only bandwidth history on the device page'
  ];

  protected readonly starterItems = [
    'Angular app boots successfully',
    'Spring Boot app boots successfully',
    'Frontend can call the starter backend endpoint',
    'Prompt log template and large dataset generator are included'
  ];

  protected readonly deliverables = [
    'Working UI and API in the boilerplate folders',
    'Prompt usage documented in prompts/prompt-log.md',
    'A short note on tradeoffs, edge cases, and performance decisions'
  ];

  protected readonly referenceSteps = [
    'Start by defining the API contracts and routes you actually want to support in the boilerplate folders.',
    'Use the overview and organization screenshots to calibrate information hierarchy, navigation depth, and summary density.',
    'Use the site and edge screenshots to calibrate drill-down depth: health rollups, metadata cards, interface sections, and WAN-only telemetry.',
    'Treat the screenshots as directional references, not as a requirement to copy the final UI exactly.'
  ];

  protected readonly referenceScreens = [
    {
      title: 'Overview Reference',
      description: 'Completed dashboard overview with summary cards, site rollups, and top-level system status.',
      src: '/final_dashboard_mock/final-overview.png',
      alt: 'Reference screenshot of the finished overview dashboard'
    },
    {
      title: 'Organization Reference',
      description: 'Organization-level screen showing grouped site data and the next navigation layer.',
      src: '/final_dashboard_mock/final-organization.png',
      alt: 'Reference screenshot of the finished organization page'
    },
    {
      title: 'Site Reference',
      description: 'Site-level screen with site health, device summaries, and branch-level operational detail.',
      src: '/final_dashboard_mock/final-site.png',
      alt: 'Reference screenshot of the finished site page'
    },
    {
      title: 'Edge Reference',
      description: 'Example of the device-level screen with interface status and WAN-only history.',
      src: '/final_dashboard_mock/final-edge.png',
      alt: 'Reference screenshot of the finished edge device page'
    }
  ];
}
