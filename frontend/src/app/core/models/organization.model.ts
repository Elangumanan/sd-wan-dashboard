export interface OrganizationSummary {
  id: string;
  name: string;
  description: string;
  region: string;
  totalSites: number;
  healthySites: number;
  degradedSites: number;
  downSites: number;
}
