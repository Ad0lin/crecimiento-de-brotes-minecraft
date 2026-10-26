# 🌳 Unrestricted Trees

**Make trees grow where Vanilla Minecraft normally won't let them!**

**¡Hacé crecer árboles donde Minecraft Vanilla normalmente no lo permitiría!**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-green)
![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![Version](https://img.shields.io/badge/Version-1.53.0-orange)

---

## 🇪🇸 Español

### 🌱 ¿Qué es Unrestricted Trees?

**Unrestricted Trees** es un mod para Minecraft Java Edition, desarrollado para permitir el crecimiento de árboles en espacios reducidos, especialmente dentro de granjas automáticas de madera.

En Minecraft Vanilla, muchos árboles se niegan a crecer cuando encuentran bloques demasiado cerca, incluso cuando esos bloques no interfieren directamente con el tronco.

Unrestricted Trees modifica determinadas comprobaciones de espacio para que los árboles puedan crecer junto a componentes de redstone, dispensadores, observadores, pistones y otros bloques de construcción.

**Su objetivo principal es permitir el crecimiento sin destruir la maquinaria existente.**

### ✨ Características

- 🌳 Permite el crecimiento de árboles en espacios reducidos.
- ⚙️ Diseñado especialmente para granjas automáticas de madera.
- 🛡️ Incorpora mecanismos de protección para evitar que la generación sustituya bloques preexistentes.
- 🌲 Compatible con árboles pequeños y gigantes, incluidos troncos rectos y ramificados.
- 🍄 Compatibilidad observada en pruebas con hongos gigantes y árboles del Nether.
- 🪓 Compatible con el datapack **Timber de Vanilla Tweaks** en las pruebas realizadas.
- 🚀 Diagnósticos de desarrollo desactivados por defecto para reducir la sobrecarga innecesaria.
- 🧩 No requiere configurar las especies de árboles individualmente.

### 🌲 Especies probadas

Durante el desarrollo se realizaron pruebas satisfactorias con:

- Roble
- Abeto (1×1 y 2×2)
- Abedul
- Jungla (1×1 y 2×2)
- Acacia
- Roble oscuro
- Roble pálido
- Cerezo
- Hongos gigantes
- Hongos del Nether (carmesí y distorsionado)

El comportamiento exacto puede variar según la disposición de los bloques y otros mods instalados.

### 📦 Instalación

1. Instalá **Fabric Loader** para Minecraft Java **1.21.11**.
2. Descargá el archivo `.jar` de Unrestricted Trees.
3. Colocalo en la carpeta `mods` de Minecraft.
4. Iniciá Minecraft con el perfil Fabric.

No es necesario activar ningún datapack ni modificar archivos de configuración para utilizar las funciones principales del mod.

### 🔧 Granjas automáticas

Unrestricted Trees **no planta retoños automáticamente** y tampoco construye granjas.

Para una granja completamente automática, vas a necesitar un sistema de plantación, como otro mod que permita a los dispensadores colocar retoños, además de los mecanismos habituales de fertilización y recolección.

Unrestricted Trees se ocupa de facilitar el crecimiento de los árboles dentro de esas estructuras.

### ⚠️ Advertencias

Aunque el mod incluye protecciones contra la sustitución de bloques y superó numerosas pruebas con maquinaria de redstone, no podemos garantizar compatibilidad absoluta con todas las construcciones, configuraciones o combinaciones de mods.

Se recomienda probar los diseños nuevos en una copia del mundo antes de utilizarlos en instalaciones importantes.

---

## 🇬🇧 English

### 🌱 What is Unrestricted Trees?

**Unrestricted Trees** is a Minecraft Java Edition mod designed to allow trees to grow in confined spaces, especially inside automated wood farms.

In Vanilla Minecraft, trees may refuse to grow when nearby blocks interfere with their space requirements, even when those blocks do not directly obstruct the main trunk.

Unrestricted Trees relaxes selected tree-generation space checks, allowing trees to grow near redstone components, dispensers, observers, pistons and other structures.

**Its main goal is to enable tree growth while preserving existing machinery.**

### ✨ Features

- 🌳 Allows tree growth in confined spaces.
- ⚙️ Designed with automated tree farms in mind.
- 🛡️ Includes protection mechanisms to prevent tree generation from replacing pre-existing blocks.
- 🌲 Supports small and giant trees, including straight and branching trunk types.
- 🍄 Successfully tested with giant mushrooms and Nether fungi.
- 🪓 Tested for compatibility with **Vanilla Tweaks Timber**.
- 🚀 Development diagnostics are disabled by default to reduce unnecessary overhead.
- 🧩 No individual tree-species configuration required.

### 🌲 Tested tree types

Successful development tests included:

- Oak
- Spruce (1×1 and 2×2)
- Birch
- Jungle (1×1 and 2×2)
- Acacia
- Dark Oak
- Pale Oak
- Cherry
- Giant Mushrooms
- Crimson and Warped Fungi

Results may depend on block placement and other installed mods.

### 📦 Installation

1. Install **Fabric Loader** for Minecraft Java **1.21.11**.
2. Download the Unrestricted Trees `.jar` file.
3. Place it inside your Minecraft `mods` folder.
4. Launch Minecraft using your Fabric profile.

No additional datapack or configuration is required for the mod's main features.

### 🔧 Automated farms

Unrestricted Trees **does not automatically plant saplings** or build farms.

Fully automated setups require a separate planting mechanism, such as another mod that enables dispensers to place saplings, along with the usual bonemeal and harvesting systems.

Unrestricted Trees focuses on allowing trees to grow within those structures.

### ⚠️ Important notes

While the mod includes block-protection mechanisms and has passed extensive tests with redstone machinery, complete compatibility with every farm design and mod combination cannot be guaranteed.

Always test new farm designs in a backup world before deploying them in important saves.

---

## 🛠️ Technical information

| Property | Value |
|---|---|
| Mod | Unrestricted Trees |
| Version | 1.53.0 |
| Minecraft | Java Edition 1.21.11 |
| Mod loader | Fabric |
| Mod ID | `unrestricted_trees` |
| Java | 21 |
| Configuration | No user configuration required |

The mod uses Fabric Mixins to adjust selected vanilla tree-generation checks and protect existing blocks during tree placement.

Debugging and diagnostic code is retained for development purposes but disabled by default.

## ❤️ Credits / Créditos

**💡 Idea, project direction and testing / Idea, dirección del proyecto y pruebas**

**Gastón Pares (Ad0lin)**

Creator of the project, responsible for its objectives, extensive in-game testing, compatibility verification and final validation.

**🤖 Main AI-assisted development / Desarrollo principal asistido por IA**

**ChatGPT (OpenAI)**

Code development assistance, debugging, technical design, analysis and iterative problem-solving throughout the project.

**✨ Initial contribution / Contribución inicial**

**Gemini AI (Google)**

Initial assistance and foundational contributions during the project's development.

This is an independently developed community project and is not officially affiliated with or endorsed by Mojang, Microsoft, OpenAI or Google.

---

## 🔗 Project links / Enlaces

- **Source code / Código fuente:** [GitHub Repository](https://github.com/Ad0lin/crecimiento-de-brotes-minecraft)
- **Issues / Errores y sugerencias:** [GitHub Issues](https://github.com/Ad0lin/crecimiento-de-brotes-minecraft/issues)

---

*Made for tree farms that refuse to let a few blocks get in the way.*

*Hecho para granjas de árboles que se niegan a dejar que unos pocos bloques les impidan crecer.* 🌳
