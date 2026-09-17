---
description: Crea un documento de especificación (feature nueva, ampliación de feature existente o PoC) a partir de un texto libre, sin incluir plan de implementación
argument-hint: [texto libre con ID, título, branch, historia de usuario, criterios de aceptación, especificaciones funcionales/no funcionales, referencias, assets]
allowed-tools: Read, Write, Glob, Grep, Bash(mkdir:*)
---

# Comando: crear-especificacion

## Propósito

Este comando recibe un texto libre (posiblemente extenso y desordenado) provisto por el usuario en `$ARGUMENTS`, y a partir de él genera **un único documento Markdown de especificación** dentro de la carpeta `specifications/` en la raíz del proyecto.

Este documento es de **QUÉ y PARA QUÉ**, nunca de **CÓMO**. Es la entrada de otro comando existente que se encarga de generar el plan de implementación. Por lo tanto:

> ⚠️ **REGLA CRÍTICA E INQUEBRANTABLE**
> Este comando NUNCA debe incluir en el documento generado: diseño técnico, arquitectura, decisiones de implementación, breakdown de tareas, estimaciones de esfuerzo, stack tecnológico a usar, pasos de desarrollo, ni ningún contenido propio de un plan de implementación. Si el texto de entrada trae ese tipo de información, se debe extraer únicamente el "para qué" (el problema/objetivo que motiva esa idea técnica) y descartar el "cómo", o como máximo mencionarlo en la sección de "Notas y supuestos" como una restricción o preferencia declarada por el interesado, jamás como una sección de diseño.

## Argumento de entrada

`$ARGUMENTS` es un bloque de texto libre que puede contener, en cualquier orden y con cualquier grado de completitud, información como:

- ID de la especificación (si el usuario ya tiene uno de su propio sistema de tracking)
- Título
- Nombre de branch de trabajo
- Historia de usuario
- Criterios de aceptación
- Especificaciones funcionales
- Especificaciones no funcionales
- Referencias a otros documentos (rutas, URLs)
- Referencias a imágenes de ejemplo / mockups / assets (rutas, URLs)
- Contexto de negocio, motivación, problema a resolver

El texto puede ser desordenado, incompleto o redactado de forma conversacional. Tu trabajo es interpretarlo, estructurarlo y completar el template. **No es tu trabajo inventar información que no está ni se puede inferir razonablemente del texto.**

## Pasos a ejecutar

### 1. Preparar el entorno

1. Verifica si existe la carpeta `specifications/` en la raíz del proyecto (usa `Glob` o `Bash(mkdir -p specifications)` si no existe, sin fallar si ya existe).
2. Lista los archivos existentes en `specifications/` (`Glob: specifications/*.md`) para:
   - Evitar colisión de nombres de archivo.
   - Evitar duplicar un ID ya usado.
   - Detectar si el texto de entrada hace referencia a una especificación ya existente (en cuyo caso, informa al usuario y pregunta si quiere modificar la existente en lugar de crear una nueva — no sobrescribas sin confirmación).

### 2. Analizar el texto de entrada

Extrae y clasifica la información del `$ARGUMENTS` en los siguientes campos. Si un campo no está presente ni es inferible, márcalo explícitamente como pendiente (ver sección 4).

| Campo | Cómo obtenerlo |
|---|---|
| `tipo` | Determina si es `nueva-feature`, `modificacion-feature` o `poc`. Búscalo explícito en el texto ("PoC", "prueba de concepto", "modificar", "ampliar", "nueva funcionalidad"). Si es ambiguo, infierelo del contexto; si no puedes inferirlo con confianza, pregunta al usuario antes de continuar. |
| `id` | Ver regla de normalización de ID en la subsección **"Regla de normalización del ID y nombre de archivo"** más abajo. |
| `titulo` | Extráelo del texto. Si no es explícito, redacta uno breve (máx. ~10 palabras) que resuma el objetivo. |
| `branch` | Si el usuario lo da, úsalo. Si no, sugiere uno siguiendo la convención `feature/<slug>`, `fix/<slug>` o `poc/<slug>` según el `tipo`, y acláralo como sugerencia. |
| `historia_usuario` | Formato "Como [rol], quiero [acción], para [beneficio]". Si el texto no lo da en ese formato, reconstrúyela a partir del contexto sin inventar el rol o beneficio si no hay pistas — en ese caso, márcalo como pendiente. |
| `contexto_motivacion` | Problema de negocio o técnico que origina el requerimiento. |
| `especificaciones_funcionales` | Lista de comportamientos/reglas que el sistema debe cumplir. |
| `especificaciones_no_funcionales` | Rendimiento, seguridad, usabilidad, accesibilidad, compatibilidad, escalabilidad, disponibilidad, cumplimiento normativo, etc. |
| `criterios_aceptacion` | En formato Given/When/Then si es posible, o como checklist verificable. |
| `alcance_incluye` / `alcance_no_incluye` | Qué está dentro y fuera del alcance. Si el texto no lo distingue explícitamente, infiere límites razonables a partir de las especificaciones funcionales y decláralo como supuesto. |
| `referencias_documentos` | Rutas o URLs a documentos relacionados mencionados en el texto. |
| `assets_visuales` | Rutas o URLs a imágenes, mockups, diagramas de ejemplo. Si son rutas locales, puedes usar `Read` para verificar que existen y describir brevemente su contenido si es una imagen. |
| `dependencias_riesgos` | Dependencias con otros sistemas/equipos/features, y riesgos identificados en el texto. |
| `stakeholders` | Roles o personas mencionadas como interesados, solicitantes o responsables de validar. |
| **Específico de PoC** `hipotesis` / `criterios_exito_fracaso` / `duracion_estimada_validacion` | Solo si `tipo = poc`. |
| **Específico de modificación** `estado_actual` | Solo si `tipo = modificacion-feature`: describe brevemente el comportamiento actual y qué cambia, en términos de comportamiento observable, NUNCA en términos de código/arquitectura actual. |

#### Regla de normalización del ID y nombre de archivo

Sin importar si el usuario provee un ID propio o no, el **nombre del archivo generado siempre debe iniciar con el prefijo `SPEC-`**, y el `id` que se guarda en el frontmatter debe ser exactamente igual al nombre del archivo (sin la extensión `.md`). Para calcularlo:

1. **Si el usuario proveyó un ID** (ej. `RAM-001`, `JIRA-1234`, `045`):
   - Primero **sanitiza y valida** el valor recibido siguiendo la subsección **"Sanitización del ID"** más abajo.
   - Sobre el valor ya sanitizado, si **ya comienza con `SPEC-`** (sin importar mayúsculas/minúsculas, ej. `spec-RAM-001`), normaliza solo el casing a `SPEC-` y no lo prefijes de nuevo.
   - Si **no** comienza con `SPEC-`, antepón el prefijo `SPEC-` tal cual al valor ya sanitizado.
   - Ejemplos:
     - Usuario da `RAM-001` → `id` final = `SPEC-RAM-001`
     - Usuario da `045` → `id` final = `SPEC-045`
     - Usuario da `spec-045` → `id` final = `SPEC-045`
     - Usuario da `SPEC-RAM-001` → `id` final = `SPEC-RAM-001` (no se duplica el prefijo)
     - Usuario da `RAM 001 (urgente)` → se sanitiza a `RAM-001-URGENTE` → `id` final = `SPEC-RAM-001-URGENTE`
2. **Si el usuario NO proveyó ningún ID**:
   - Genera un identificador con formato `AAAAMMDD-NN`, donde `NN` es un consecutivo de dos dígitos calculado a partir de los archivos ya existentes en `specifications/` para ese mismo día.
   - Antepón el prefijo: `id` final = `SPEC-AAAAMMDD-NN`.
3. El **nombre de archivo siempre es** `{id}.md` (el `id` ya calculado, incluyendo el prefijo `SPEC-`, sin agregar slug del título ni ningún otro sufijo).
4. Antes de escribir el archivo, verifica contra el listado de `specifications/*.md` que ese nombre no exista ya:
   - Si coincide exactamente con un archivo existente, no lo sobrescribas: informa al usuario y pregunta si desea versionar/modificar la especificación existente o usar otro ID.

#### Sanitización del ID

El ID provisto por el usuario puede venir mal escrito para usarse como nombre de archivo (con espacios, tildes, símbolos, etc.). Antes de anteponerle `SPEC-`, aplica estas reglas en orden:

1. Quita espacios al inicio/final (`trim`).
2. Reemplaza tildes/diacríticos por su equivalente sin tilde (ej. `RAMÓN` → `RAMON`).
3. Convierte cualquier espacio interno en guion (`-`).
4. Elimina cualquier carácter que **no** sea letra (A-Z/a-z), número (0-9) o guion (`-`). Esto descarta símbolos como `()`, `#`, `_`, `/`, `.`, `@`, emojis, etc. (Si el símbolo eliminado era relevante, ej. `#`, no lo reconstruyas; simplemente omítelo).
5. Colapsa guiones consecutivos (`--`, `---`) en uno solo.
6. Quita guiones al inicio o al final del resultado.
7. Si tras sanitizar el resultado queda **vacío**, o si el ID original era ambiguo/no confiable (ej. era solo símbolos), trata el caso como si el usuario **no** hubiera provisto ID (aplica la regla 2 de generación automática) y acláralo en la respuesta final al usuario.
8. Conserva el casing (mayúsculas/minúsculas) tal como quedó tras los pasos anteriores; no fuerces mayúsculas ni minúsculas sobre el ID del usuario (solo el prefijo `SPEC-` siempre va en mayúsculas).
9. Si el resultado sanitizado difiere notablemente del valor original dado por el usuario, menciónalo explícitamente en la respuesta final (paso 5 del flujo) para que el usuario confirme que el ID final es el esperado.

> Esta misma sanitización aplica también si el usuario da el ID ya con el prefijo `SPEC-` incluido (ej. `SPEC RAM 001` → sanitizado a `SPEC-RAM-001`).

> El campo `titulo` sigue existiendo y se usa únicamente para el encabezado `# {titulo}` dentro del documento y para la respuesta final al usuario — ya no se usa para construir el nombre del archivo.

### 3. Generar el documento

Crea el archivo en:

```
specifications/{id}.md
```

Con el siguiente contenido y estructura (usa exactamente este orden de secciones; omite las secciones marcadas como condicionales cuando no apliquen al `tipo`):

```markdown
---
id: {id}
titulo: "{titulo}"
tipo: {tipo}
estado: borrador
fecha_creacion: {fecha_actual_ISO}
branch: {branch}
stakeholders: [{lista}]
version: 0.1.0
---

# {titulo}

## 1. Resumen ejecutivo
Breve párrafo (3-5 líneas) que resume qué se quiere lograr y por qué, entendible por alguien no técnico.

## 2. Contexto y motivación
Descripción del problema, oportunidad o necesidad de negocio que origina esta especificación.

## 3. Historia de usuario
> Como {rol}, quiero {accion}, para {beneficio}.

## 4. Alcance
### Incluye
- ...
### No incluye
- ...

## 5. Especificaciones funcionales
Lista numerada de comportamientos y reglas de negocio que el sistema debe cumplir.

## 6. Especificaciones no funcionales
Rendimiento, seguridad, usabilidad, accesibilidad, disponibilidad, compatibilidad, cumplimiento normativo, etc. (solo las que apliquen).

## 7. Criterios de aceptación
Lista en formato Given/When/Then o checklist verificable.

## 8. Estado actual y cambio propuesto  <!-- SOLO si tipo = modificacion-feature -->
Descripción del comportamiento actual (observable, no técnico) y qué cambia.

## 9. Hipótesis y criterios de éxito de la PoC  <!-- SOLO si tipo = poc -->
- Hipótesis a validar
- Criterios de éxito / fracaso
- Duración estimada de la validación (si se indicó)

## 10. Dependencias y riesgos
Dependencias con otros equipos, sistemas o features, y riesgos conocidos.

## 11. Referencias
### Documentos relacionados
- ...
### Assets visuales / mockups / ejemplos
- ...

## 12. Supuestos y preguntas abiertas
- Supuestos asumidos por no estar explícitos en el texto original.
- Preguntas pendientes de responder por los stakeholders antes de aprobar la especificación.

## 13. Historial de cambios
| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | {fecha_actual} | Versión inicial generada a partir de la solicitud del usuario |
```

Reglas de redacción del contenido:

- Redacta en español, en tono claro y profesional, orientado a que cualquier stakeholder (técnico o no) entienda el requerimiento.
- Sé fiel al texto original: no agregues alcance, reglas de negocio o criterios que el usuario no mencionó ni se infieren razonablemente.
- Cuando falte información relevante para una sección, no la dejes vacía en silencio: escribe explícitamente `_Pendiente de definir por el interesado._` y añade la pregunta correspondiente en la sección 12.
- No incluyas nombres de clases, funciones, endpoints, tablas de base de datos, librerías, ni ninguna decisión de diseño técnico, salvo que el usuario las haya dado como una restricción de negocio explícita (ej. "debe integrarse con el sistema X vía su API existente" es una dependencia válida de mencionar; "usar Redis para cachear" es una decisión técnica que NO debe incluirse).

### 4. Validaciones antes de finalizar

Antes de dar por terminado el documento, revisa:

- [ ] ¿El documento contiene alguna sección de diseño técnico, arquitectura o plan de tareas? → Si sí, elimínala o reescríbela en términos de requerimiento.
- [ ] ¿Todas las secciones obligatorias (1, 2, 3, 4, 5, 7, 11, 12, 13) están presentes, aunque sea con "Pendiente de definir"?
- [ ] ¿El nombre de archivo inicia con `SPEC-`, coincide exactamente con el `id` del frontmatter, y no colisiona con una especificación ya existente en `specifications/`?
- [ ] Si el usuario proveyó un ID propio, ¿se sanitizó correctamente (sin espacios, tildes, símbolos ni guiones duplicados) y se respetó su contenido y casing, solo con el prefijo `SPEC-` normalizado y sin agregarle un slug del título?
- [ ] Si la sanitización modificó el ID original de forma notable, ¿se lo hiciste saber al usuario en la respuesta final?
- [ ] ¿Los criterios de aceptación son verificables (se puede decir objetivamente si se cumplen o no)?

### 5. Respuesta final al usuario

Después de crear el archivo, responde de forma breve (sin repetir todo el contenido del documento):

1. Ruta del archivo creado.
2. Tipo de especificación detectado (`nueva-feature` / `modificacion-feature` / `poc`) y el ID asignado (indicando si fue sanitizado respecto al valor original dado por el usuario, o generado automáticamente por no haberse provisto uno).
3. Lista corta de supuestos hechos y preguntas pendientes (si las hay), para que el usuario las confirme o resuelva.
4. Recordatorio de que este documento es solo la especificación, y que el plan de implementación se genera con el otro comando existente para ese fin.

No uses la herramienta `present_files` fuera de este flujo de Claude Code; simplemente confirma la ruta relativa del archivo dentro del repositorio.
