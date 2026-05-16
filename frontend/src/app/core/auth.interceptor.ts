import { HttpInterceptorFn } from '@angular/common/http';

const TOKEN_KEY = 'retours_access_token';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (request.url.includes('/auth/login')) {
    return next(request);
  }

  const token = localStorage.getItem(TOKEN_KEY);

  if (!token) {
    return next(request);
  }

  const cloned = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(cloned);
};
