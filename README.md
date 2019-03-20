# re-om [![Clojars Project](https://img.shields.io/clojars/v/greenpowermonitor/re-om.svg)](https://clojars.org/greenpowermonitor/re-om)

A re-frame inspired Om framework for both writing new SPAs
and giving new life to existing legacy SPAs.

## Install

Add `[greenpowermonitor/re-om "0.1.0-SNAPSHOT"]` to `[:dependencies]` in your `project.clj`.

## Rationale

### Why re-om?

#### 1. The problem with horizon: imperative programming everywhere.
We are working on a legacy ClojureScript SPA that uses [Om](https://github.com/omcljs/om).

This SPA might have had some kind of architecture or design in some moment in the past but technical debt, lack of documentation and design flaws conflated **business logic** (pure logic that decides how to interact with the user or data transformations) and **effectful code**, which were not clearly separated.

This **lack of separation of concerns** was making the SPA hard to evolve because its code was **difficult to understand and to test, and hence to change**. This difficulty in understanding and testing the code was a high barrier to test that resulted in a very low test coverage that amplified even more the problems to evolve the code safely and at a sustainable pace. This generated **a vicious circle**.

Even more, **conflating pure and effectful code destroys the advantages of functional programming**. It doesn't matter that we're using a language like Clojure, **without clear isolation between pure and effectful code, you'll end up doing [imperative programming](https://en.wikipedia.org/wiki/Imperative_programming)**.

#### 2. A possible solution: effects and coeffects.
Using **effects and coeffects** it is a way of getting the **separation of concerns** we were lacking. They help achieve **a clear isolation of effectful code and business logic** that **makes the interesting logic pure** and, as such, **really easy to test and reason about**. With them we can really enjoy the advantages of functional programming.

Any piece of logic using a design based on effects and coeffects is comprised of three parts:

1. Extracting all the needed data from "the world" (using coeffects for getting application state, getting component state, getting DOM state, etc).
2. Using pure functions to compute the description of the side effects to be performed (returning effects for updating application state, sending messages, etc) given what was extracted from "the world" in the previous step (the coeffects).
3. Performing the side effects described by the effects returned by the pure functions executed in the previous step.

At the beginning, when `re-om` wasn't yet accepted by everyone in the team, we used some examples in horizon which where creating these coeffects and effects manually, (have a look at [Improving legacy Om code (I)](https://www.codesai.com/2018/07/improving-legacy-om-code-1) and [Improving legacy Om code (II)](https://www.codesai.com/2018/07/improving-legacy-om-code-2)), but this can get cumbersome quickly.

#### 3. An event-driven framework with effects and coeffects: `re-frame`.

Some of us had worked with effects and coeffects before while developing SPAs with [re-frame](https://github.com/Day8/re-frame) and had experienced how good it is. After working with `re-frame`, when you come to horizon, you realize how a good architecture can make a dramatic difference in clarity, testability, understandability and easiness of change.

Having a framework like `re-frame` removes most of the boilerplate of working with effects and coeffects, creating clear boundaries and constraints that separate pure code from effectful code and gives you a very clear flow to add new functionality that's very easy to test and protect from regressions. In that sense `re-frame`'s architecture can be considered what [Jeff Atwood](https://blog.codinghorror.com/) defined as [**a pit of success**](https://blog.codinghorror.com/falling-into-the-pit-of-success/) because it is:

> **“a design that makes it easy to do the right things and annoying (but not impossible) to do the wrong**
> **things.”**

#### 4. Why not use `re-frame` then?

in principle using `re-frame` in our SPA would have been wonderful, but in practice this was never really an option.

A very important premise for us was that **a rewrite was out of the question because we would have been blocked from producing any new feature for too much time. We needed to continue developing new features**. So we decided we would follow the [strangler application pattern](https://www.martinfowler.com/bliki/StranglerApplication.html), an approach which would allow us to progressively evolve our SPA to use an architecture like `re-frame`'s one, while being able to keep adding new features all the time. The idea is that all new code would use the new architecture, if it were pragmatically possible, and that we would change bit by bit only the legacy parts that we had to change. This means that during a, probably long, period of time inside the SPA, the new architecture would have to coexist with the old imperative way of coding.

In principle, following the **strangler application pattern** was not incompatible with introducing `re-frame`, but let’s examine more closely what starting to use `re-frame` would have meant to us:

##### 4. 1. From Om to reagent.
`re-frame` uses [reagent](https://github.com/reagent-project/reagent) as its interface to `React`. Although I personally consider `reagent` to be much nicer than `Om` because it feels more ‘Clo­jur­ish’, as it is less verbose and hides `React`’s complexity better that `Om` ([`Om` it’s a thinner abstraction over `React` that `reagent`](http://theatticlight.net/posts/Om-and-Reagent/)), the amount of view code and components developed using `Om` during the nearly two years of life of the project made changing to `reagent` too huge of a change. GreenPowerMonitor had done a heavy investment on `Om` in our SPA for this change to be advisable.

If we had chosen to start using `re-frame`, we would have faced a huge amount of work. Even following a **strangler application pattern**, it would have taken quite some time to abandon `Om`, and in the meantime `Om` and `reagent` would have had to coexist in our code base. This coexistence would have been problematic because we’d have had to either rewrite some components or add complicated wrapping to reuse `Om` components from `reagent` ones. It would have also forced our developers to learn and develop with both technologies.

Those reasons made us abandon the idea of using `re-frame`, and chose a less risky and progressive way to get `our real goal`, which was **having the advantages of `re-frame`’s architecture in our code**.

#### 5. re-om is born.

We decided to do a spike to write an event-driven framework system with effects and coeffects. After having a look at have a look at `re-frame`'s code it turned out it wouldn't be too big of an undertaking. Once we had it done, we called it `re-om` as a joke.

At the beginning we had only events with effects and coeffects and started to try it in our code. From the very beginning we saw great improvements in testability and understandability of the code. This original code that was independent of any view technology was improved during several months of use. Most of this code ended being part of [reffectory](https://github.com/GreenPowerMonitor/reffectory).

Later our colleague [Joel Sánchez](https://github.com/JoelSanchez) **added subscriptions to `re-om`**. This radically changed the way we approach the development of components. They started to become **dumb view code with nearly no logic** inside, which started to make cumbersome component integration testing nearly unnecessary. Another surprising effect of using `re-om` was that we were also able to have less and less state inside controls which made things like validations or transformation of the state of controls comprised of other controls much easier.

A really important characteristic of **`re-om` is that it’s not invasive**. Since it was **thought from the very beginning to retrofit an event-driven architecture with an effects and coeffects system into legacy SPAs**, it’s ideal when you want to evolve a code base gradually following a **strangler application pattern**. The only thing we need to do is initialize `re-om` passing horizon's `app-state` atom. From then on, `re-om` subscriptions will detect any changes made by the legacy imperative code to re-render the components subscribed to them, and it'll also be able to use effect handlers we wrote on top of it to mutate the `app-state` using horizon `lenses` and do other effects that "talk" to the legacy part.

This way we could start carving islands of pure functional code inside horizon's imperative soup, and introduced some sanity making horizon's development more sustainable.


## Documentation

[re-om's architecture](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/architecture.md)

[Built-in Coeffects](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/built-in-coeffects.md)

[Built-in Effects](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/built-in-effects.md)

[Registering custom coeffect handlers](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/custom-coeffects.md)

We'll grow re-om's documentation bit by bit in the following months.

## License

Copyright © 2018 GreenPowerMonitor

Distributed under the Eclipse Public License version 1.0.
