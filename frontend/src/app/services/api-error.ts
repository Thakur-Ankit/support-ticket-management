import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorBody } from '../models/ticket.models';

export interface ParsedApiError {
  code: string;
  message: string;
  fieldErrors: Record<string, string>;
}

export function parseApiError(err: unknown): ParsedApiError {
  if (!(err instanceof HttpErrorResponse)) {
    return { code: 'UNKNOWN', message: 'Unexpected error', fieldErrors: {} };
  }

  const body = err.error as ApiErrorBody | null;
  if (body && typeof body === 'object' && body.message) {
    const fieldErrors: Record<string, string> = {};
    (body.details || []).forEach((d) => {
      if (d.field) {
        fieldErrors[d.field] = d.message;
      }
    });
    return {
      code: body.error || 'ERROR',
      message: body.message,
      fieldErrors,
    };
  }

  return {
    code: 'HTTP_' + err.status,
    message: err.message || 'Request failed',
    fieldErrors: {},
  };
}
