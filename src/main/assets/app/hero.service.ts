import { Injectable } from 'angular2/core';
import { Http, Response, Headers, RequestOptions  } from 'angular2/http';
import { Observable } from 'rxjs/Observable';

import './rxjs-operators'; // TODO: no debería ser necesaria esta linea porque ya esta en "app.component.ts", pero si la quito falla el sbt-typescript cuando esta en modo watch (~run o ~re-start)

import { Hero } from './hero';

@Injectable()
export class HeroService {
    private heroesUrl = 'app/heroes';  // URL to web api 

    constructor(private http: Http) { }

    getHeroes(): Observable<Hero[]> {
        return this.http.get(this.heroesUrl)
            .map((response: Response) => response.json().wrappedArray)
            .catch(this.handleError);
    }

    // Add new Hero
    private post(hero: Hero): Observable<Hero> {
        let headers = new Headers({
            'Content-Type': 'application/json'
        });

        return this.http
            .post(this.heroesUrl, JSON.stringify(hero), { headers: headers })
            .map((response: any) => response.json())
            .catch(this.handleError);
    }

    // Update existing Hero
    private put(hero: Hero): Observable<Hero> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');

        let url = `${this.heroesUrl}/${hero.id}`;

        return this.http
            .put(url, JSON.stringify(hero), { headers: headers })
            .map(() => hero)
            .catch(this.handleError);
    }

    delete(hero: Hero): Observable<any> {
        let headers = new Headers();
        headers.append('Content-Type', 'application/json');

        let url = `${this.heroesUrl}/${hero.id}`;

        return this.http
            .delete(url, headers)
            .catch(this.handleError);
    }

    save(hero: Hero): Observable<Hero> {
        if (hero.id)
            return this.put(hero);
        else
            return this.post(hero);
    }

    getHero(id: number): Observable<Hero> {
        return this.getHeroes()
            .map((heroes:Hero[]) => heroes.filter(hero => hero.id === id)[0]);
    }

    private handleError(error: any) {
        // In a real world app, we might use a remote logging infrastructure
        // We'd also dig deeper into the error to get a better message
        let errMsg = (error.message) ? error.message : (error.status ? `${error.status} - ${error.statusText}` : 'Server error');
        console.error('An error occurred', errMsg); // log to console instead
        return Observable.throw(errMsg);
    }
}