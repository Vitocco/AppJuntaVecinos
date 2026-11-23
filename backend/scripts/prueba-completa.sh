#!/bin/bash
# =====================================================================
# Prueba de punta a punta del backend Junta Zaror (Git Bash / Linux / macOS)
#
#   bash scripts/prueba-completa.sh         -> todo vía gateway (8080). Sirve con Docker o con el IDE.
#   bash scripts/prueba-completa.sh --ide   -> además prueba llamar DIRECTO a los ms (8082, 8083).
#                                              Solo con los ms corriendo desde el IDE: en Docker
#                                              esos puertos no se exponen (a propósito).
#
# Requisitos: backend levantado, curl y openssl (vienen con Git Bash), y el .env en la raíz.
# Deja registros de prueba en la BD (reserva CANCELADA, movimiento ANULADO, un usuario e informe).
# =====================================================================

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
if [ ! -f "$RAIZ/.env" ]; then echo "Falta $RAIZ/.env (cópialo desde .env.example)"; exit 1; fi
set -a; . "$RAIZ/.env"; set +a

G=http://localhost:8080/api
J='Content-Type: application/json; charset=UTF-8'
MODO_IDE=false; [ "$1" = "--ide" ] && MODO_IDE=true
D=$(mktemp -d); cd "$D" || exit 1       # archivos temporales con rutas relativas (curl de Windows)
OK=0; FAIL=0; RUN=$(date +%s)

chk() { # chk "<descripcion>" <esperado> <obtenido>
  if [ "$2" = "$3" ]; then OK=$((OK+1)); printf '  OK   %-3s %s\n' "$3" "$1"
  else FAIL=$((FAIL+1)); printf '  FAIL %-3s (esperado %s) %s\n' "$3" "$2" "$1"; fi
}
code() { curl -s -o out -w '%{http_code}' "$@"; }
contiene() { grep -q "$1" out && echo 1 || echo 0; }
tok() { curl -s -X POST $G/auth/login -H "$J" -d "{\"email\":\"$1\",\"password\":\"1234\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'; }
A() { echo "Authorization: Bearer $1"; }
b64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }
jwt() { # jwt '<payload>' '<secreto>' -> token HS256 firmado
  local h p s; h=$(printf '%s' '{"alg":"HS256"}' | b64url); p=$(printf '%s' "$1" | b64url)
  s=$(printf '%s' "$h.$p" | openssl dgst -sha256 -hmac "$2" -binary | b64url); echo "$h.$p.$s"; }

echo "Esperando al gateway..."
for i in $(seq 1 30); do [ "$(code -X POST $G/auth/login -H "$J" -d '{"email":"x@x.cl","password":"x"}')" = "401" ] && break; sleep 3; done

echo "== AUTH"
chk "POST /auth/login (camila)"            200 "$(code -X POST $G/auth/login -H "$J" -d '{"email":"camila@zaror.cl","password":"1234"}')"
chk "POST /auth/login password mala"       401 "$(code -X POST $G/auth/login -H "$J" -d '{"email":"camila@zaror.cl","password":"x"}')"
chk "POST /auth/login body inválido"       400 "$(code -X POST $G/auth/login -H "$J" -d '{"email":"no"}')"
printf '%s' "{\"nombre\":\"Prueba Script\",\"email\":\"prueba.$RUN@zaror.cl\",\"telefono\":\"+56900000000\",\"numeroCasa\":\"99\",\"fechaNacimiento\":\"1990-01-01\",\"password\":\"1234\",\"recibirAvisos\":false}" > reg.json
chk "POST /auth/register"                  200 "$(code -X POST $G/auth/register -H "$J" --data-binary @reg.json)"
chk "  -> crea rol VECINO"                 1   "$(contiene '"rol":"VECINO"')"
chk "POST /auth/register email repetido"   409 "$(code -X POST $G/auth/register -H "$J" --data-binary @reg.json)"

TC=$(tok camila@zaror.cl); TL=$(tok luis@zaror.cl); TD=$(tok directiva@zaror.cl); TT=$(tok tesorero@zaror.cl)

echo "== SEGURIDAD JWT (vía gateway)"
NOW=$(date +%s)
VENCIDO=$(jwt "{\"sub\":\"camila@zaror.cl\",\"id\":3,\"rol\":\"VECINO\",\"nombre\":\"Camila Rojas\",\"iat\":$((NOW-7200)),\"exp\":$((NOW-60))}" "$JWT_SECRET")
OTRA_FIRMA=$(jwt "{\"sub\":\"camila@zaror.cl\",\"id\":3,\"rol\":\"DIRECTIVA\",\"nombre\":\"x\",\"iat\":$NOW,\"exp\":$((NOW+3600))}" "otro-secreto-cualquiera-de-32-caracteres!!")
H=$(echo "$TC" | cut -d. -f1); S=$(echo "$TC" | cut -d. -f3)
P=$(printf '%s' "{\"sub\":\"camila@zaror.cl\",\"id\":3,\"rol\":\"DIRECTIVA\",\"nombre\":\"Camila Rojas\",\"iat\":$NOW,\"exp\":$((NOW+3600))}" | b64url)
ROL_ALTERADO="$H.$P.$S"
ALG_NONE="$(printf '%s' '{"alg":"none"}' | b64url).$P."
chk "sin token"                            401 "$(code $G/usuarios/me)"
chk "header sin 'Bearer '"                 401 "$(code $G/usuarios/me -H "Authorization: $TC")"
chk "token vencido"                        401 "$(code $G/usuarios/me -H "$(A $VENCIDO)")"
chk "firmado con otro secreto"             401 "$(code $G/reservas -H "$(A $OTRA_FIRMA)")"
chk "rol cambiado a DIRECTIVA sin refirmar" 401 "$(code $G/reservas -H "$(A $ROL_ALTERADO)")"
chk "alg none"                             401 "$(code $G/reservas -H "$(A $ALG_NONE)")"
chk "/interno vía gateway (aunque traiga la clave)" 403 "$(code $G/usuarios/interno/1 -H "X-Internal-Key: $INTERNAL_KEY")"
chk "/interno vía gateway (reservas)"      403 "$(code $G/reservas/interno/2)"
chk "el id sale del token (me de luis)"    200 "$(code $G/usuarios/me -H "$(A $TL)")"
chk "  -> id 4"                            1   "$(contiene '"id":4')"

echo "== USUARIOS"
chk "GET /usuarios/me"                     200 "$(code $G/usuarios/me -H "$(A $TC)")"
printf '%s' '{"nombre":"Camila Rojas","telefono":"+56933333333","numeroCasa":"45","recibirAvisos":true}' > perfil.json
chk "PUT /usuarios/me"                     200 "$(code -X PUT $G/usuarios/me -H "$(A $TC)" -H "$J" --data-binary @perfil.json)"

echo "== SEDES"
chk "GET /sedes"                           200 "$(code $G/sedes -H "$(A $TC)")"
chk "GET /sedes sin token"                 401 "$(code $G/sedes)"
chk "GET /sedes/1/disponibilidad"          200 "$(code "$G/sedes/1/disponibilidad?anio=2026&mes=11" -H "$(A $TC)")"
chk "  -> sin datos personales"            0   "$(contiene 'usuario')"
chk "GET /sedes/99/disponibilidad"         404 "$(code "$G/sedes/99/disponibilidad?anio=2026&mes=11" -H "$(A $TC)")"
chk "GET /sedes/fotos/README.md (pública)" 200 "$(code $G/sedes/fotos/README.md)"
chk "GET /sedes/fotos/no-existe.jpg"       404 "$(code $G/sedes/fotos/no-existe.jpg)"

echo "== RESERVAS"
printf '%s' '{"sedeId":2,"fecha":"2027-01-15","horaInicio":"10:00","horaFin":"13:00","motivo":"Taller de repostería","asistentes":10}' > res.json
chk "POST /reservas"                       201 "$(code -X POST $G/reservas -H "$(A $TL)" -H "$J" --data-binary @res.json)"
RID=$(sed -E 's/^\{"id":([0-9]+).*/\1/' out)
chk "POST /reservas tope con APROBADA"     409 "$(code -X POST $G/reservas -H "$(A $TL)" -H "$J" -d '{"sedeId":1,"fecha":"2026-11-28","horaInicio":"16:00","horaFin":"18:00","motivo":"x","asistentes":5}')"
chk "GET /reservas/mias"                   200 "$(code $G/reservas/mias -H "$(A $TL)")"
chk "GET /reservas (directiva)"            200 "$(code $G/reservas -H "$(A $TD)")"
chk "GET /reservas?estado=PENDIENTE (tesorero)" 200 "$(code "$G/reservas?estado=PENDIENTE" -H "$(A $TT)")"
chk "GET /reservas (vecino)"               403 "$(code $G/reservas -H "$(A $TC)")"
chk "PUT /reservas/{id}/estado (tesorero)" 403 "$(code -X PUT $G/reservas/$RID/estado -H "$(A $TT)" -H "$J" -d '{"estado":"APROBADA"}')"
chk "PUT /reservas/{id}/estado (directiva)" 200 "$(code -X PUT $G/reservas/$RID/estado -H "$(A $TD)" -H "$J" -d '{"estado":"APROBADA","observacion":"OK"}')"
chk "DELETE /reservas/{id} (no dueño)"     403 "$(code -X DELETE $G/reservas/$RID -H "$(A $TC)")"
chk "DELETE /reservas/{id} (dueño)"        200 "$(code -X DELETE $G/reservas/$RID -H "$(A $TL)")"

echo "== FINANZAS"
chk "GET /finanzas/resumen (vecino)"       200 "$(code "$G/finanzas/resumen?anio=2026&mes=10" -H "$(A $TC)")"
chk "  -> saldo octubre 376.800"           1   "$(contiene '"saldo":376800')"
chk "GET /categorias (tesorero)"           200 "$(code $G/categorias -H "$(A $TT)")"
chk "GET /categorias (directiva)"          403 "$(code $G/categorias -H "$(A $TD)")"
chk "GET /movimientos (directiva)"         200 "$(code "$G/movimientos?anio=2026&mes=11" -H "$(A $TD)")"
chk "GET /movimientos (vecino)"            403 "$(code "$G/movimientos?anio=2026&mes=11" -H "$(A $TC)")"
echo 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==' | base64 -d > boleta.png
printf '%s' '{"tipo":"GASTO","categoriaId":9,"descripcion":"Prueba automática","monto":1000,"fechaMovimiento":"2026-11-16"}' > mov.json
chk "POST /movimientos (multipart + foto)" 201 "$(code -X POST $G/movimientos -H "$(A $TT)" -F "datos=@mov.json;type=application/json" -F "archivo=@boleta.png;type=image/png")"
MID=$(sed -E 's/^\{"id":([0-9]+).*/\1/' out)
chk "PUT /movimientos/{id}"                200 "$(code -X PUT $G/movimientos/$MID -H "$(A $TT)" -H "$J" --data-binary @mov.json)"
chk "GET /movimientos/{id}/comprobante"    200 "$(code $G/movimientos/$MID/comprobante -H "$(A $TD)")"
chk "DELETE /movimientos/{id} -> ANULADO"  200 "$(code -X DELETE $G/movimientos/$MID -H "$(A $TT)")"
chk "GET /pagos (directiva)"               200 "$(code $G/pagos -H "$(A $TD)")"
chk "GET /pagos (vecino)"                  403 "$(code $G/pagos -H "$(A $TC)")"
printf '%s' '{"reservaId":4,"monto":35000,"medio":"EFECTIVO","fechaPago":"2026-11-18"}' > pago.json
chk "POST /pagos (reserva rechazada)"      409 "$(code -X POST $G/pagos -H "$(A $TT)" -F "datos=@pago.json;type=application/json" -F "archivo=@boleta.png;type=image/png")"
chk "POST /pagos/webpay/iniciar (no dueño)" 403 "$(code -X POST $G/pagos/webpay/iniciar -H "$(A $TL)" -H "$J" -d '{"reservaId":2}')"
chk "POST /pagos/webpay/iniciar (dueño)"   200 "$(code -X POST $G/pagos/webpay/iniciar -H "$(A $TC)" -H "$J" -d '{"reservaId":2}')"
WT=$(sed -E 's/.*"token":"([^"]+)".*/\1/' out)
chk "GET /pagos/webpay/retorno sin JWT (anulado)" 200 "$(code "$G/pagos/webpay/retorno?TBK_TOKEN=$WT&TBK_ORDEN_COMPRA=x&TBK_ID_SESION=y")"
chk "  -> redirige a zaror://pago?estado=error" 1 "$(contiene 'zaror://pago?estado=error')"
chk "GET /informes (vecino)"               200 "$(code $G/informes -H "$(A $TC)")"
chk "GET /informes/1"                      200 "$(code $G/informes/1 -H "$(A $TC)")"
printf '%s' "{\"titulo\":\"Prueba script $RUN\",\"periodoInicio\":\"2026-11-01\",\"periodoFin\":\"2026-11-30\",\"publicado\":false}" > inf.json
chk "POST /informes (vecino)"              403 "$(code -X POST $G/informes -H "$(A $TC)" -H "$J" --data-binary @inf.json)"
chk "POST /informes (tesorero)"            201 "$(code -X POST $G/informes -H "$(A $TT)" -H "$J" --data-binary @inf.json)"

if $MODO_IDE; then
  echo "== SOLO IDE: llamadas directas a los ms (saltándose el gateway)"
  chk "token vencido directo a ms-usuario (8082)" 401 "$(code localhost:8082/usuarios/me -H "$(A $VENCIDO)")"
  chk "ms-reserva directo sin token (8083)"       401 "$(code localhost:8083/reservas/mias)"
  chk "ms-reserva directo con token válido"       200 "$(code localhost:8083/reservas/mias -H "$(A $TC)")"
  chk "/interno directo con JWT (sin clave)"      403 "$(code localhost:8083/reservas/interno/2 -H "$(A $TD)")"
  chk "/interno directo con clave equivocada"     401 "$(code localhost:8082/usuarios/interno/1 -H 'X-Internal-Key: mala')"
  chk "/interno directo con clave correcta"       200 "$(code localhost:8082/usuarios/interno/1 -H "X-Internal-Key: $INTERNAL_KEY")"
fi

echo
echo "RESULTADO: $OK OK, $FAIL FAIL"
cd / && rm -rf "$D"
[ "$FAIL" -eq 0 ]
