# Proyecto Final: Buscador de Rutas Óptimas en Mapas Urbanos

![alt text](assets/LogoUniversidad.png)

**Universidad Politécnica Salesiana**
**Carrera:** Computación
**Asignatura:** Estructura de Datos
**Estudiantes:** Geovanny Cabrera y Martin Amaya.
**Fecha:**  9 de Febrero de 2026

---

## Índice
1. [Descripción del Problema](#1-descripción-del-problema)
2. [Objetivo](#2-objetivo)
3. [Marco Teórico y Algoritmia](#3-marco-teórico-y-algoritmia)
4. [Arquitectura de Software (MVC)](#4-arquitectura-de-software-mvc)
5. [Estructura del Proyecto](#5-estructura-del-proyecto)
6. [Funcionalidades y Funcionamiento](#6-funcionalidades-y-funcionamiento)
7. [Análisis de Resultados (Conclusiones)](#7-análisis-de-resultados-conclusiones)
8. [Recomendaciones](#8-recomendaciones)

---

## 1. Descripción del Problema
La optimización de trayectorias en entornos urbanos es un desafío clásico de la teoría de grafos. Este proyecto aborda la simulación de navegación usando en este caso el mapa de **Las Vegas**, modelando su infraestructura vial como un **grafo no dirigido**.El sistema permite la digitalización manual de intersecciones (nodos) y calles (aristas) sobre una proyección satelital, resolviendo el problema de encontrar rutas entre dos puntos mediante estrategias de búsqueda ciega.

## 2. Objetivo
Desarrollar una aplicación de software que implemente y visualice gráficamente los algoritmos **BFS (Búsqueda en Anchura)** y **DFS (Búsqueda en Profundidad)**, permitiendo analizar comparativamente su eficiencia, orden de exploración y pertinencia en la resolución de rutas sobre mapas reales, garantizando la persistencia de datos y una experiencia de usuario interactiva.

---

## 3. Marco Teórico y Algoritmia

### Representación del Grafo
El sistema implementa un modelo matemático $G = (V, E)$, donde $V$ son las intersecciones y $E$ las conexiones viales. La estructura subyacente utiliza listas de adyacencia para optimizar el rendimiento en memoria.

### Algoritmos Implementados

#### BFS (Breadth-First Search)
Estrategia de exploración por niveles concéntricos. Utiliza una estructura **FIFO (Cola)** para visitar todos los nodos vecinos inmediatos antes de profundizar.
* **Comportamiento:** Expansión radial uniforme ("onda").
* **Garantía:** En grafos no ponderados, asegura matemáticamente encontrar el camino con el menor número de aristas (ruta más corta).

#### DFS (Búsqueda en Profundidad)
Estrategia de exploración intensiva. Utiliza una estructura **LIFO (Pila)** para avanzar por una rama hasta agotarla antes de realizar *backtracking*.
* **Comportamiento:** Avance agresivo y sinuoso.
* **Propiedad:** No garantiza la ruta óptima y tiende a generar caminos redundantes en mallas urbanas densas.

---

## 4. Arquitectura de Software (MVC)

El proyecto sigue el patrón **Modelo-Vista-Controlador** para desacoplar la lógica matemática de la interfaz de usuario.

* **Modelo (`logic`):** Contiene la clase `PathFinder` (motor algorítmico puro) y las entidades del grafo (`NodeView`, `EdgeView`). No tiene dependencias de la interfaz gráfica.
* **Vista (`view`):** Gestionada por `MainFrame` y `GraphPanel`. Este último implementa un motor de renderizado personalizado con `Graphics2D` y manejo de eventos de ratón.
* **Controlador:** Orquesta la interacción entre las entradas del usuario (clics, botones) y la actualización del modelo de datos.

---

## 5. Funcionalidades y Funcionamiento

### 5.1 Funcionalidades de Edición (v1.1)
Se mantienen las herramientas de digitalización desarrolladas en la fase inicial:

**A. Interfaz Adaptativa**
* **Pantalla Completa:** La aplicación inicia maximizada para aprovechar el área de trabajo.
* **Escalado Inteligente:** Algoritmo de reescalado que ajusta la imagen del mapa a la resolución del monitor sin perder proporción ni calidad.

**B. Editor de Grafos**
* **Modo "Crear Nodos":** Interacción directa (clic) sobre el mapa para definir intersecciones.
* **Modo "Conectar Nodos":** Herramienta visual para unir nodos (Origen -> Destino), representando las calles.

**C. UX y Validaciones**
* **Cancelación:** Clic derecho para abortar una conexión en curso.
* **Integridad:** Validación lógica que impide conectar un nodo consigo mismo (bucles).
* **Feedback Visual:** El cursor cambia a `HAND_CURSOR` al pasar sobre un nodo interactivo.

### 5.2 Simulación Algorítmica (v2.0)
El núcleo del proyecto permite la ejecución y análisis de los algoritmos de búsqueda:

* **Selección de Ruta:** Definición visual de nodos de Inicio y Fin.
* **Animación de Exploración:** Visualización en tiempo real (color amarillo) del orden en que el algoritmo "descubre" el grafo.
* **Trazado de Ruta:** Renderizado final del camino encontrado (color rojo) tras reconstruir los punteros de los nodos padres.
* **Registro de Métricas:** Generación automática del archivo `reporte_tiempos.csv` con el algoritmo usado, tiempo en nanosegundos y pasos totales.

---

## 6. Análisis de Resultados (Conclusiones)

Tras realizar múltiples pruebas de navegación sobre la malla y analizar los datos recolectados en el archivo CSV:

**Eficacia de Ruta (Optimización):**
* **BFS** se consolida como la estrategia adecuada para la navegación. Al explorar por niveles, garantiza encontrar la ruta con el menor número de intersecciones posibles.
* **DFS** demostró ser ineficiente para rutas óptimas. Aunque encuentra caminos válidos, las rutas generadas suelen dar rodeos innecesarios ("serpenteo") antes de converger al destino.

**Rendimiento Computacional:**
* Los tiempos registrados oscilan entre **40.000ns y 140.000ns**, lo que confirma que ambos algoritmos son extremadamente ligeros para la escala del mapa actual.
* **DFS** presenta una alta varianza: puede ser instantáneo si la rama elegida aleatoriamente contiene el destino, o el más lento si debe realizar mucho *backtracking*. **BFS** mantiene tiempos más constantes y predecibles.

---

## 7. Recomendaciones

* **Escalabilidad Algorítmica:** Se recomienda implementar el patrón de diseño *Strategy* para facilitar la incorporación futura de algoritmos informados como **A* (A-Star)**, necesarios si se desea considerar la distancia física real (metros) en lugar de solo la topología.
* **Heurísticas:** Para mapas de gran escala, migrar de BFS a A* utilizando la distancia euclidiana como heurística reduciría significativamente el espacio de búsqueda explorado.
* **Visualización de Datos:** Incorporar un panel lateral de estadísticas en tiempo real (nodos visitados, tiempo, longitud de ruta) eliminaría la dependencia de revisar el archivo CSV externo para el análisis inmediato.

## 8. Capturas Y Evidencia

#### Funcionalidad v1.1
![alt text](assets/PreVisualizacion(v1.1).png)

#### Simulación Algorítmica v2.0 (BFS)
![alt text](assets/Previsualizacion(BFS).png)

#### Simulación Algorítmica v2.0 (DFS)
![alt text](assets/Previsualizacion(DFS).png)





