import { Component, OnInit } from 'angular2/core';
import { Router } from 'angular2/router';

import {Hero} from './hero';
import {HeroService} from './hero.service';


@Component({
    selector: 'my-dashboard',
    templateUrl: 'assets/app/dashboard.component.html',
    styleUrls: ['assets/app/dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    constructor(private heroService: HeroService, private router: Router) { }
    heroes: Hero[];

    ngOnInit() {
        this.heroService.getHeroes().subscribe(heroes => this.heroes = heroes.slice(0, 5));
    }

    gotoDetail(hero: Hero) {
        let link = ['HeroDetail', { id: hero.id }];
        this.router.navigate(link);
    }
}