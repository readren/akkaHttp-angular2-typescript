import { Component, EventEmitter, Input, OnInit, Output } from 'angular2/core';
import { RouteParams } from 'angular2/router';

import {Hero} from "./hero";
import { HeroService } from './hero.service';

@Component({
	selector: 'my-hero-detail',
	templateUrl: 'assets/app/hero-detail.component.html',
	styleUrls: ['assets/app/hero-detail.component.css']
})
export class HeroDetailComponent implements OnInit {
	@Input() private hero: Hero;
	@Output() close = new EventEmitter();
	error: any;
	private navigated = false; // true if navigated here

	constructor(private heroService: HeroService, private routeParams: RouteParams) { }

	ngOnInit() {
		let rawId = this.routeParams.get('id');
		if (rawId !== null) {
			let id = +rawId;
			this.navigated = true;
			this.heroService.getHero(id).subscribe(
				hero => this.hero = hero,
				error => this.error = error);
		} else {
			this.navigated = false;
			this.hero = new Hero();
		}
	}

	save() {
		this.heroService.save(this.hero)
			.subscribe(hero => {
				this.hero = hero;
				this.goBack(hero);
			},
			error => this.error = error); // TODO: display error message
	}

	goBack(savedHero: Hero = null) {
		this.close.emit(savedHero);
		if (this.navigated) { window.history.back(); }
	}
}