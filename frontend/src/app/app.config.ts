import { ApplicationConfig, isDevMode } from '@angular/core';
import { PreloadAllModules, provideRouter, withPreloading } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { routes } from './app.routes';
import { apiInterceptor } from './core/interceptors/api.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Routing — preload all lazy bundles in the background after initial load.
    provideRouter(routes, withPreloading(PreloadAllModules)),

    // HTTP — attach the API interceptor globally.
    provideHttpClient(withInterceptors([apiInterceptor])),

    // NgRx — root store with no reducers; feature states register themselves lazily.
    provideStore({}),
    provideEffects([]),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() }),
  ],
};
