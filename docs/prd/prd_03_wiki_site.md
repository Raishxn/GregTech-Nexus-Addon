# 🌐 PRD #3 — GTNA Wiki / Documentation Site
## GregTech Nexus Addon — Documentation Site Requirements
### Versão 1.0 | Bilíngue PT-BR / EN

---

## 1. Visão Geral / Overview

**PT-BR**: O site de documentação do GTNA será hospedado no GitHub Pages (gratuito) e servirá como referência completa para jogadores e desenvolvedores. Segue o padrão do [GT Modern Docs](https://gregtechceu.github.io/GregTech-Modern/1.20.1/) mas com melhorias significativas em UX.

**EN**: The GTNA documentation site will be hosted on GitHub Pages (free) and serve as a complete reference for players and developers. It follows the [GT Modern Docs](https://gregtechceu.github.io/GregTech-Modern/1.20.1/) pattern but with significant UX improvements.

---

## 2. Technology Stack

| Componente | Tecnologia | Justificativa |
|-----------|-----------|---------------|
| **Framework** | MkDocs | Mesmo que GT Modern, facilita familiaridade |
| **Theme** | Material for MkDocs | Premium, busca integrada, i18n, dark mode |
| **Linguagem** | Markdown + YAML | Simples de editar para contribuidores |
| **Deploy** | GitHub Actions → [GitHub Pages] | Automático, gratuito |
| **Diagramas** | Mermaid | Integrado no Material theme |
| **Search** | Algolia ou lunr.js (built-in) | Busca rápida e precisa |
| **i18n** | Plugin i18n do Material | Inglês (default) + Português |

### Setup Inicial

```yaml
# mkdocs.yml
site_name: GregTech Nexus Addon
site_url: https://raishxn.github.io/GregTech-Nexus-Addon/
site_description: "Documentation for GregTech Nexus Addon - The Nexus of Steam & Steel"
repo_url: https://github.com/Raishxn/GregTech-Nexus-Addon
repo_name: Raishxn/GregTech-Nexus-Addon

theme:
  name: material
  language: en
  palette:
    - scheme: slate
      primary: deep orange
      accent: amber
      toggle:
        icon: material/brightness-4
        name: Switch to light mode
    - scheme: default
      primary: deep orange
      accent: amber
      toggle:
        icon: material/brightness-7
        name: Switch to dark mode
  features:
    - navigation.sections
    - navigation.tabs
    - navigation.top
    - navigation.expand
    - search.highlight
    - search.suggest
    - content.code.copy
    - content.tabs.link
  icon:
    repo: fontawesome/brands/github
  logo: assets/logo.png
  favicon: assets/favicon.png

plugins:
  - search
  - i18n:
      default_language: en
      languages:
        en: English
        pt-BR: Português (Brasil)
  - tags

markdown_extensions:
  - admonition
  - pymdownx.details
  - pymdownx.superfences:
      custom_fences:
        - name: mermaid
          class: mermaid
          format: !!python/name:pymdownx.superfences.fence_mermaid
  - pymdownx.tabbed:
      alternate_style: true
  - pymdownx.emoji
  - attr_list
  - md_in_html
  - tables

extra:
  social:
    - icon: fontawesome/brands/github
      link: https://github.com/Raishxn/GregTech-Nexus-Addon
    - icon: fontawesome/brands/discord
      link: https://discord.gg/d3qHufwRxb
```

---

## 3. Estrutura do Site / Site Structure

```
docs/
├── index.md                        ← Home page
├── getting-started/
│   ├── installation.md             ← Instalação e requisitos
│   ├── first-steps.md              ← Primeiros passos
│   └── faq.md                      ← Perguntas frequentes
│
├── gameplay/                       ← SEÇÃO JOGADORES
│   ├── eras/
│   │   ├── overview.md             ← Mapa de progressão
│   │   ├── steam-and-steel.md      ← Era do Vapor & Aço
│   │   ├── hydraulic.md            ← Era Hidráulica
│   │   └── supercritical.md        ← Era Supercrítica
│   │
│   ├── machines/
│   │   ├── index.md                ← Lista de todas as máquinas
│   │   ├── steam/                  ← Multiblocos a Vapor
│   │   │   ├── forge-iron-crown.md
│   │   │   ├── steam-crystallizer.md
│   │   │   ├── pneumatic-washer.md
│   │   │   ├── steam-distillation.md
│   │   │   ├── hydraulic-press.md
│   │   │   ├── large-steam-furnace.md
│   │   │   ├── large-steam-crusher.md
│   │   │   ├── large-steam-alloy-smelter.md
│   │   │   ├── mega-solar-boiler.md
│   │   │   ├── steam-cobbler.md
│   │   │   ├── stone-superheater.md
│   │   │   ├── steam-manufacturer.md
│   │   │   └── steam-woodcutter.md
│   │   ├── electric/               ← Multiblocos Elétricos
│   │   │   ├── nexus-reactor.md
│   │   │   └── industrial-slaughterhouse.md
│   │   └── parts/                  ← Hatches e Partes
│   │       ├── wireless-steam.md
│   │       ├── pattern-buffer.md
│   │       ├── thread-hatch.md
│   │       ├── accelerate-hatch.md
│   │       ├── overclock-hatch.md
│   │       └── advanced-parallel.md
│   │
│   ├── materials/
│   │   ├── index.md                ← Lista de materiais
│   │   ├── elements.md             ← Elementos customizados
│   │   ├── alloys.md               ← Ligas metálicas
│   │   └── fluids.md               ← Fluidos
│   │
│   ├── systems/
│   │   ├── wireless-steam.md       ← Sistema Wireless Steam
│   │   ├── pattern-buffer.md       ← Pattern Buffer detalhado
│   │   ├── hydraulic.md            ← Sistema Hidráulico
│   │   └── wall-sharing.md         ← Wall Sharing expandido
│   │
│   └── tips/
│       ├── optimization.md         ← Dicas de otimização
│       ├── ae2-integration.md      ← Integração com AE2
│       └── common-mistakes.md      ← Erros comuns
│
├── development/                    ← SEÇÃO DESENVOLVEDORES
│   ├── setup.md                    ← Setup do ambiente
│   ├── architecture.md             ← Arquitetura do código
│   ├── api/
│   │   ├── overview.md             ← API Overview
│   │   ├── machines.md             ← Como criar máquinas
│   │   ├── materials.md            ← Como criar materiais
│   │   ├── recipes.md              ← Como criar receitas
│   │   └── pattern-buffer-api.md   ← API do Pattern Buffer
│   ├── contributing.md             ← Guia de contribuição
│   └── changelog.md                ← Changelog
│
└── assets/
    ├── logo.png
    ├── favicon.png
    ├── images/
    │   ├── machines/               ← Screenshots de máquinas
    │   ├── materials/              ← Ícones de materiais
    │   └── diagrams/               ← Diagramas
    └── css/
        └── custom.css              ← Estilos customizados
```

---

## 4. Template de Página por Tipo

### 4.1 Template: Página de Máquina

```markdown
# [Nome da Máquina] / [Machine Name]

!!! quote "Lore"
    "[Lore text in English]"
    "[Lore text in Portuguese]"

## Overview

[Descrição de 2-3 parágrafos explicando o que a máquina faz e por que é útil]

## Key Stats

| Propriedade | Valor |
|------------|-------|
| **Tipo** | Multiblock (Steam) |
| **Tamanho** | WxHxD |
| **Paralelismo** | N |
| **Velocidade** | Nx base |
| **Steam/tick** | N L/t |
| **Era** | [Era Name] |

## Construction

### Required Materials

| Material | Amount |
|----------|--------|
| ... | ... |

### Structure Layout

=== "Layer 1 (Bottom)"
    ```
    AAAA
    ABBA
    ABBA
    AAAA
    ```

=== "Layer 2"
    ```
    ...
    ```

[Screenshot/diagram da estrutura formada]

## Recipes & Usage

### Example Recipes

[Tabela de receitas mais usadas nesta máquina]

### Best Practices

!!! tip "Optimization Tips"
    [3-5 dicas específicas para esta máquina]

## Special Mechanics

### [Mechanic Name]
[Explicação detalhada]

### Efficiency Calculations

[Fórmulas com exemplos numéricos]

## FAQ

??? question "Pergunta comum 1?"
    Resposta detalhada.

??? question "Can I use this with [X]?"
    [Compatibility info]
```

### 4.2 Template: Página de Material

```markdown
# [Material Name]

## Properties

| Propriedade | Valor |
|------------|-------|
| **Fórmula** | [Formula] |
| **Cor** | [Color swatch + hex] |
| **Blast Temp** | [K] |
| **Hazardous** | [Yes/No + details] |
| **Era** | [Era] |

## How to Obtain

[Step-by-step com receitas]

## Used In

[Lista de máquinas e receitas que usam este material]

## Color Palette

[Swatches visuais: base, dark, light]
```

---

## 5. Design Specifications

### Paleta de Cores do Site

```css
/* Custom GTNA Theme Colors */
:root {
  --gtna-primary: #FF6B00;         /* Deep Orange — Forge/Steam */
  --gtna-secondary: #FFA726;       /* Amber — QoL/Info */
  --gtna-accent: #968030;          /* Stronze Gold — Premium */
  --gtna-dark-bg: #1a1a2e;         /* Deep Navy — Background */
  --gtna-code-bg: #16213e;         /* Code blocks */
  --gtna-steam: #C0C0C0;           /* Steam grey */
  --gtna-nexium: #4A7AE5;          /* Nexium blue */
  --gtna-echoite: #26734d;         /* Echoite green */
  --gtna-danger: #FF3333;          /* Hazardous red */
}
```

### Typography
- **Headings**: Inter (Google Fonts)
- **Body**: Inter
- **Code**: JetBrains Mono

---

## 6. Deployment

### GitHub Actions Workflow

```yaml
# .github/workflows/docs.yml
name: Deploy Docs
on:
  push:
    branches: [main]
    paths: ['docs/**', 'mkdocs.yml']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: 3.x
      - run: pip install mkdocs-material mkdocs-i18n
      - run: mkdocs gh-deploy --force
```

---

## 7. Content Guidelines

### Para Jogadores
- **Linguagem**: Casual mas precisa
- **Sempre inclua**: "Por que construir?" em cada página de máquina
- **Sempre inclua**: Exemplos concretos com números
- **Use admonitions**: `!!! tip`, `!!! warning`, `!!! example`
- **Screenshots**: Mínimo 1 por página de máquina

### Para Desenvolvedores
- **Linguagem**: Técnica, com code blocks
- **Sempre inclua**: Código funcional que pode ser copiado
- **Links para source**: Cada API reference deve linkar ao código real
- **Versioning**: Documente breaking changes claramente

---

*PRD do Website GTNA — Documentação no GitHub Pages*
