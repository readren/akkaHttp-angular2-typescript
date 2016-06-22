import { Component, OnInit } from 'angular2/core';
import { Router } from 'angular2/router';

import { Hero } from './hero';
import { HeroService } from './hero.service';
import {HeroDetailComponent } from './hero-detail.component';

@Component({
	moduleId: 'assets/app/heroes.component',
	selector: 'my-heroes',
	directives: [HeroDetailComponent],
	templateUrl: 'heroes.component.html',
	styleUrls: ['heroes.component.css']
})
export class HeroesComponent implements OnInit {
	heroes: Hero[];
	selectedHero: Hero;
	private addingHero: boolean = false;
	private error: any;

	constructor(private heroService: HeroService, private router: Router) { }

	ngOnInit() {
		this.initHeroes();
	}

	initHeroes() {
		this.heroService.getHeroes()
			.subscribe(
				heroes => this.heroes = heroes,
				error => this.error = error); // TODO: Display error message;
	}

	onSelect(hero: Hero) {
		this.selectedHero = hero;
		this.addingHero = false;
	}

	gotoDetails() {
		let link = ['HeroDetail', { id: this.selectedHero.id }];
		this.router.navigate(link);
	}

	addHero() {
		this.addingHero = true;
		this.selectedHero = null;
	}

	close(savedHero: Hero) {
		this.addingHero = false;
		if (savedHero) { this.initHeroes(); }
	}

	delete(hero: Hero, event: any) {
		event.stopPropagation();
		this.heroService.delete(hero)
			.subscribe(res => {
				this.heroes = this.heroes.filter(h => h !== hero);
				if (this.selectedHero === hero)
					this.selectedHero = null;
			},
			error => this.error = error); // TODO: display error message
	}
}


