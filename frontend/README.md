# Frontend

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 22.1.8.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Testes

Os testes do frontend são **Robot Framework**, no mesmo padrão LKDF (POM → FLOW → SCENARIO → TEST) e na
mesma suíte dos testes de API, em [`../test/robot/`](../test/robot/README.md). O navegador é controlado pela
Browser library, que roda o Playwright por baixo. Detalhes na
[ADR-0077](../docs/adr/0077-testes-de-frontend-com-robot-framework.md).

`ng test` e `ng e2e` não são usados como teste de aceitação das telas. Toda tela nova ou refeita traz o
teste Robot de interface no mesmo PR.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
