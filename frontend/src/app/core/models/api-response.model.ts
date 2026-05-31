/** Matches the backend SuccessResponse<T> envelope. */
export interface SuccessResponse<T> {
  value: T;
  message?: string;
}

/** Matches the backend ErrorResponse envelope. */
export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: string[] | Record<string, unknown>;
}
