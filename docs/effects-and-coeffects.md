# Effects and Coeffects
##Effects.
They **describe your program’s side-effects**, (*what your program does to the world*).

##Coeffects.
They  **track your program’s side-causes**, (*what your program requires from the world*).

## Declarative Effects pattern
* **Event handlers are pure functions**.
*  They **receive coeffects** and **return effects**.
*  Something else (a language run-time, a framework, etc.) is in charge of **injecting the values that the coeffects track** and **interpreting the effects data to actually perform the side-effects they describe**.
* That something else would be **re-om** in our case.

[Built-in Coeffects in re-om](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/built-in-coeffects.md)

[Built-in Effects in re-om](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/built-in-effects.md)


[Registering custom coeffect handlers](https://github.com/GreenPowerMonitor/re-om/blob/master/docs/custom-coeffects.md)