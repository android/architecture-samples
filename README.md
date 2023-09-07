# ENGLISH🇺🇸

# Android Architecture Samples

These samples showcase different architectural approaches to developing Android apps. In its different branches you'll find the same app (a TODO app) implemented with small differences.

In this branch you'll find:
*   User Interface built with **[Jetpack Compose](https://developer.android.com/jetpack/compose)** 
*   A single-activity architecture, using **[Navigation Compose](https://developer.android.com/jetpack/compose/navigation)**.
*   A presentation layer that contains a Compose screen (View) and a **ViewModel** per screen (or feature).
*   Reactive UIs using **[Flow](https://developer.android.com/kotlin/flow)** and **[coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** for asynchronous operations.
*   A **data layer** with a repository and two data sources (local using Room and a fake remote).
*   Two **product flavors**, `mock` and `prod`, [to ease development and testing](https://android-developers.googleblog.com/2015/12/leveraging-product-flavors-in-android.html).
*   A collection of unit, integration and e2e **tests**, including "shared" tests that can be run on emulator/device.
*   Dependency injection using [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).

## Variations

This project hosts each sample app in separate repository branches. For more information, see the `README.md` file in each branch.

### Stable samples - Kotlin
|     Sample     | Description |
| ------------- | ------------- |
| [main](https://github.com/googlesamples/android-architecture/tree/main) | This branch |
| [service-locator](https://github.com/googlesamples/android-architecture/tree/service-locator) | A simple setup that removes Hilt in favor of a service locator |
| [livedata](https://github.com/googlesamples/android-architecture/tree/livedata) | Uses LiveData instead of StateFlow as the data stream solution |
| [usecases](https://github.com/googlesamples/android-architecture/tree/usecases) | Adds a new domain layer that uses UseCases for business logic (not using Compose yet) |
| [views](https://github.com/googlesamples/android-architecture/tree/views) | Uses Views instead of Jetpack Compose to render UI elements on the screen |
| [views-hilt](https://github.com/googlesamples/android-architecture/tree/views-hilt) | Uses Views and Hilt instead together |


## Screenshots

<img src="screenshots/screenshots.png" alt="Screenshot">

## Why a to-do app?

The app in this project aims to be simple enough that you can understand it quickly, but complex enough to showcase difficult design decisions and testing scenarios. For more information, see the [app's specification](https://github.com/googlesamples/android-architecture/wiki/To-do-app-specification).

## What is it not?
*   A template. Check out the [Architecture Templates](https://github.com/android/architecture-templates) instead.
*   A UI/Material Design sample. The interface of the app is deliberately kept simple to focus on architecture. Check out the [Compose Samples](https://github.com/android/compose-samples) instead.
*   A complete Jetpack sample covering all libraries. Check out [Android Sunflower](https://github.com/googlesamples/android-sunflower) or the advanced [GitHub Browser Sample](https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample) instead.
*   A real production app with network access, user authentication, etc. Check out the [Now in Android app](https://github.com/android/nowinandroid) instead.

## Who is it for?

*   Intermediate developers and beginners looking for a way to structure their app in a testable and maintainable way.
*   Advanced developers looking for quick reference.

## Opening a sample in Android Studio

To open one of the samples in Android Studio, begin by checking out one of the sample branches, and then open the root directory in Android Studio. The following series of steps illustrate how to open the [usecases](tree/usecases/) sample.

Clone the repository:

```
git clone git@github.com:android/architecture-samples.git
```
This step checks out the master branch. If you want to change to a different sample: 

```
git checkout usecases
```

**Note:** To review a different sample, replace `usecases` with the name of sample you want to check out.

Finally open the `architecture-samples/` directory in Android Studio.

### License


```
Copyright 2022 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership. The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```


# Português do Brasil🇧🇷

# Exemplos da Arquitetura Android

Esses exemplos mostram a diferentes abordagens de arquitetura para o desenvolvimento de aplicativos Android. Em suas diferentes branches você vai encontrar o mesmo aplicativo (um TODO app) implementado com pequenas diferenças.

Nesta branch você vai encontrar: 
*   Interface de Usuário construída com **[Jetpack Compose](https://developer.android.com/jetpack/compose)** 
*   Uma arquitetura de atividade única, usando **[Navigation Compose](https://developer.android.com/jetpack/compose/navigation)**.
*   Uma camada de apresentação contendo uma Compose Screen (View) e uma **ViewModel** por tela (ou feature)
*   UIs reativas usando **[Flow](https://developer.android.com/kotlin/flow)** e **[coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** para operações assíncronas.
*   Uma **data layer** (camada de dados) com um repositório e duas fontes de dados (local usando Room e um fake remote, controle remoto falso) 
*   Dois **product flavors** (sabores de produtos) `mock` e `prod`, [para facilitar o desenvolvimento e os testes](https://android-developers.googleblog.com/2015/12/leveraging-product-flavors-in-android.html).
*   Uma coleção de **testes** unitários, de integração e e2e, inlcuindo testes "compartilhados" que podem ser executados no emulador/dispositivo.
*  Injeção de dependência usando [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).

## Variations (Variações)

This project hosts each sample app in separate repository branches. For more information, see the `README.md` file in each branch.
Neste projeto você acessa cada aplicativo de exemplo em branches de repositório separadas para mais informações, veja o arquivo `README.md` em cada branch.

### Stable samples - Kotlin
|     Sample     | Description |
| ------------- | ------------- |
| [main](https://github.com/googlesamples/android-architecture/tree/main) | Esta branch |
| [service-locator](https://github.com/googlesamples/android-architecture/tree/service-locator) | Uma configuração simples que remove o Hilt em favor de um serviço de localização |
| [livedata](https://github.com/googlesamples/android-architecture/tree/livedata) | Use LiveData em vez de StateFlow como uma solução de fluxo de dados |
| [usecases](https://github.com/googlesamples/android-architecture/tree/usecases) | Adiciona uma nova camanda de domínio que usa UseCases (casos de uso) para a lógica de negócio (ainda não usa o Compose)|
| [views](https://github.com/googlesamples/android-architecture/tree/views) | Usa Views em vez de Jetpack Compose para renderizar elementos da UI na tela |
| [views-hilt](https://github.com/googlesamples/android-architecture/tree/views-hilt) | Usa Views e Hilt juntos |


## Screenshots

<img src="screenshots/screenshots.png" alt="Screenshot">

## Por que um to-do app?

O aplicativo neste projeto pretende ser simples o suficiente para que você possa entendê-lo rapidamente, mas complexo o suficiente para mostrar decisões difíceis de design e cenários de teste. Para mais informações, consulte as [especificações do app](https://github.com/googlesamples/android-architecture/wiki/To-do-app-specification).

## O que não é?
*   Um template (modelo). Veja [Templates de Arquitetura](https://github.com/android/architecture-templates).
*   Um exemplo de UI/Material Design. A interface do aplicativo é simples para se ter foco maior na arquitetura. Veja [Exemplos de Compose](https://github.com/android/compose-samples) instead.
*   Um exemplo completo do Jetpack cobrindo todas as bibliotecas. Confira [Android Sunflower](https://github.com/googlesamples/android-sunflower) or the acesse [GitHub Browser Sample](https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample) em vez disso.
*   Um aplicativo de produção real com acesso a internet, autenticação do usuário, etc. Veja mais em [Agora no aplicativo Android](https://github.com/android/nowinandroid) em vez disso.

## Para quem é?

*   Desenvolvedores intermediários e iniciantes que procuram uma maneira de estruturar seus aplicativos de maneira testável e sustentável.
*   Desenvolvedores avançados em busca de referência rápida.

## Abrindo um exemplo no Android Studio

Para abrir um dos exemplos no Android Studio, comece verificando uma das branches de exemplo e, em seguida, abra o diretório raiz no Android Studio. A série de etapas a seguir mostra como abrir um exemplo [usecases](tree/usecases/).

Clone o repositório:

```
git clone git@github.com:android/architecture-samples.git
```
Esta etapa verifica o branch master. Se você quiser mudar para uma amostra diferente:
```
git checkout usecases
```

**Observação:** Para revisar um exemplo diferente, substitua `usecases` pelo nome do exemplo que você deseja verificar.

Por fim, abra o diretório `architecture-samples/` no Android Studio.

### Licença


```
Direitos Autorais 2022 Google, Inc.

Licenciado para a Apache Software Foundation (ASF) sob um
ou mais contribuidores contratos de licença.
Veja o arquivo NOTICE distribuído com este trabalho para informações adicionais
sobre a propriedade dos direitos autorais.
A ASF licencia este arquivo para você
sob a Licença Apache, Versão 2.0 (a "Licença");
talvez você não use este arquivo exceto
em conformidade com a Licença. Você pode obter uma cópia a Licença em:

http://www.apache.org/licenses/LICENSE-2.0

O que é exigido pela lei aplicável ou acordado por escrito,
o software distribuído sob a Licença é
distribuído "COMO ESTÁ", SEM GARANTIAS OU CONDIÇÕES DE QUALQUER TIPO,
expressas ou implícitas. Veja a Licença
para o idioma específico que rege as permissões e limitações sob a licença.
```
