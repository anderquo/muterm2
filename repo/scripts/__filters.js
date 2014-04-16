var filters = {

	i: {
		all: {},
		init: function(){ for (var k in filters.i.all) filters.i.all[k].init(); },
	},

	o: {
		all: {},
		init: function(){ for (var k in filters.o.all) filters.o.all[k].init(); },
	},
	
	init: function(){ filters.i.init(); filters.o.init(); },
	
	props: {
	},

};

