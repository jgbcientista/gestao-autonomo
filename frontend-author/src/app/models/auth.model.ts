export interface Auth {
}

export interface AuthenticationRequest {
  email: string;
  password: string;
  ipAddress?: string;
  userAgent?: string;
  location?: string;
}

export interface AuthenticationResponse {
  token: string;
  name: string;
  email: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  roles?: string[];
}

export interface User {
  id?: number;
  name: string;
  email: string;
  roles?: string[];
}
