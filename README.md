# Proyecto Final: Buscador de Rutas Óptimas en Mapas Urbanos

**Universidad Politécnica Salesiana** **Carrera:** Computación  
**Asignatura:** Estructura de Datos  
**Estudiante:** Geovanny Alejandro Cabrera Tapia

---

## 1. Descripción del Problema

El proyecto consiste en desarrollar una aplicación que modele un mapa de calles real (Las Vegas) como un grafo. El sistema permite definir intersecciones (nodos) y calles (aristas) sobre una imagen satelital para posteriormente encontrar la ruta más eficiente entre dos puntos.

## 2. Objetivo

Implementar una herramienta visual que permita:

1.  Visualizar un mapa real (Las Vegas).
2.  Digitalizar intersecciones y conexiones manualmente.
3.  Visualizar la ejecución de algoritmos de búsqueda (BFS y DFS).

## 3. Tecnologías Utilizadas

- **Lenguaje:** Java
- **Interfaz Gráfica:** Java Swing

## 4. Funcionalidades Implementadas (v1.1)

### A. Interfaz Adaptativa

- **Pantalla Completa:** La aplicación inicia maximizada.
- **Escalado Inteligente:** El mapa se ajusta automáticamente a la resolución del monitor del usuario.

### B. Editor de Grafos

1.  **Modo "Crear Nodos":** Clic en el mapa para añadir intersecciones.
2.  **Modo "Conectar Nodos":** Clic en origen (verde) y destino para crear calles.

### C. UX y Validaciones

- **Cancelación:** Clic derecho para cancelar una selección.
- **Integridad:** Bloqueo para evitar conectar un nodo consigo mismo.
- **Cursor Interactivo:** El puntero cambia a mano al pasar sobre un nodo.
#### Avances V1.1
![alt text](assets/PreVisualizacion(v1.1).png)