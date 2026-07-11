BEGIN;

UPDATE incidentes
SET fecha_resolucion = fecha
WHERE estado = 'RESUELTO'
  AND fecha_resolucion IS NULL;

COMMIT;
