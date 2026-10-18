const ORIGENES_BACKEND_PROPIO = [8080];
const puertoActual = Number(window.location.port);
const esLocalhost = ["localhost", "127.0.0.1"].includes(window.location.hostname);

export const API_URL = "";

function getToken() {
    return localStorage.getItem("token");
}

export function authHeaders() {
    const token = getToken();
    const headers = { "Content-Type": "application/json" };
    if (token) headers.Authorization = `Bearer ${token}`;
    return headers;
}

export async function sesionActiva() {

    try {

        const response = await fetch(
            API_URL + "/usuarios/me",
            {
                headers: authHeaders()
            }
        );

        if (!response.ok) {
            return null;
        }

        return await response.json();

    } catch (error) {
        return null;
    }
}

export const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function cerrarSesion() {
    localStorage.removeItem("token");
    window.location.href = "../login-folder/Login.html";
}
//Esta mierda es una checkea el status
export async function checkRes(response) {
    if (!response.ok) {
        const text = await response.text();
        const msg = text || "Error desconocido";
        window.location.href = `../error-folder/Error.html?msg=${encodeURIComponent(msg)}`;
        throw new Error(msg);
    }
    return response;
}

export async function apiFetch(url, options = {}) {
    const res = await fetch(url, options);
    return checkRes(res);
}

export function formatearFechaLocal(isoStr) {
    if (!isoStr) return "";
    const partes = isoStr.split("T");
    const [y, m, d] = partes[0].split("-");
    if (!partes[1]) {
        return new Date(+y, +m - 1, +d).toLocaleDateString("es-AR");
    }
    const [h, min, s] = partes[1].split(":");
    const date = new Date(+y, +m - 1, +d, +h, +min, +(s || 0));
    return date.toLocaleString("es-AR");
}