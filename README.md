#Akka-Http Angular2 Typescript sample application 

This is an activator template that generates a sample Akka-Http Angular2 Typescript 1.8 application with a tutorial. It is identical to [Play Angular2 Typescript](https://github.com/readren/akkaHttp-angular2-typescript) except that uses akka-http instead of play.

It features an Angular2 application with Typescript compilation integrated with the continuous compilation of Scala code. The Typescript code is linted with `tslint`.

##Installation
Once you have [activator](https://www.typesafe.com/community/core-tools/activator-and-sbt) installed you can run `activator new akkaHttp-angular2-typescript` and you'll have a local application with a tutorial. Or you can just clone this repo and run `sbt ~run`.

**NB**: Make sure you don't have `typescript` installed globally. If you do have a global npm installation of `typescript` that version will be picked up. And then all bets are off.
A symptom of having an older global `typescript` installation is that you get a `JsTaskFailure` / `TypeError` that the function `convertCompilerOptionsFromJson` can't be found. See [this issue](https://github.com/readren/akkaHttp-angular2-typescript/issues/1)

If you want to explore the upcoming typescript 2.0 you can find that in branch `ts-20-preview`. Specifically the resolution of webjar modules is done by core ts module resolution. Which should be more robust.

##Getting started
The client side of this application is a copy of the sample used in the "tour of heroes" tutorial from the [official angular 2 documentation](https://angular.io/docs/ts/latest/tutorial/).
The server side is a simple web server implemented with akka-http. 

To start the web server:
1) Go to the project root folder with the cd command, type activator, and wait the project builder initialization. This may take several minutes the first time.
2) Then type compile and wait the builder builds the project. This also may take several minutes the first time.
3) Then type re-start to start the web server. If everything is fine, the last line in the console should say "listening to http://localhost:9001".
4) Open a modern web browser and type "http://localhost:9001" in the url address edit box. If everything is ok, you should see the "tour of heroes" tutorial application running.

This project shows 3 ways of loading the client app in the browser:  
1. let the browser load the typescript files and have them compiled in the browser itself. This is easy to setup. But it makes greater computation demands on the client device. And it is really hard to find out about compilation errors. Which rather defies the added value of typed programming that typescript provides. This is implemented in [this html file](https://github.com/readren/akkaHttp-angular2-typescript/blob/master/src/main/public/views/index.scala.html).
2. let the SBT compile the typescript files when they're changed. The browser will load all the resulting individual js files individually. That can quickly lead to slow initial loading as the number of ts files of your application increases. This is implemented in [this html file](https://github.com/readren/akkaHttp-angular2-typescript/blob/master/src/main/public/views/index1.scala.html)
3. let the SBT compile the typescript files into one single javascript file. This will load much quicker. This is implemented in [this html file](https://github.com/readren/akkaHttp-angular2-typescript/blob/master/src/main/public/views/index2.scala.html)

For a lot of production applications option 3 will be required. While option 2 is nicer for development. 
We can do both without changing our source code by using `sbt ~run` for the former and `sbt stage -DtsCompileMode=stage` for the latter. So to get option 3 to work you'll have to provide that `-DtsCompileMode=stage` jvm argument.


##what to do if
- "I've created the application through activator ui and the client browser you opened to connect to the application shows a message saying that the resource can't not be found":
This is a [known problem](https://github.com/sbt/sbt-core-next/issues/9) with activator. To solve it, close the activator ui and try to run this application using the activator interactive shell. But before that, remove the file "&lt;project-base&gt;/project/sbt-ui.sbt" or comment its contents. Said file is added by activator during the template instantiation to activate the sbt-core-next plugin required by the activator ui. Unfortunately this plugin causes the classpath be incomplete at runtime, impeding the application to obtain the resources it needs.

- "I've created the application through activator and it runs fine in activator but it hangs when I try to run it through sbt":
This is a [known problem](https://github.com/typesafehub/activator/issues/1036) with activator. Activator generates a file `project\play-fork-run.sbt` that causes this. If you remove it or comment out its contents the application will run in sbt.

##Credits
All credits are for [Joost de Vries](https://github.com/joost-de-vries) who did the [Play Angular2 Typescript](https://github.com/readren/akkaHttp-angular2-typescript) which is the base of this project. Everything is identical except the web server technology used: akka-http instead of play.
