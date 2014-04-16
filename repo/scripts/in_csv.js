//CSV filter.
//in: CSV strings, out: linear chart.
filters.i.all.csv = {
    enabled: false,
    
    name:  "CSV",
    descr: "CSV chart",
    
    pageType: "chart",

    sep: ';',
    
    format: '1,2',
    formatArray: [],
    parseFormat: function() {
        var ar = [];
        var fas = filters.i.all.csv.format.trim().split(';');
        for (var i in fas) {
            var fasn = fas[i].split(',');
            if (fasn.length == 2) ar.push( [parseInt(fasn[0]), parseInt(fasn[1])] );
        }
        filters.i.all.csv.formatArray = ar;
        
        //refresh charts if format changed
        if (filters.i.all.csv.charts != ar.length) {
            filters.i.all.csv.charts = ar.length;
        }
        
        return ar;
    },

    lines: 0, //number of parsed lines
    line: [], //current line
    
    //initialize
    init: function() {
        filters.i.all.csv.parseFormat();
    },
    
    //parse bytes_in
    update: function() {
        if (filters.i.all.csv.enabled) {
            for (var i in bytesIn) {
                var b = bytesIn[i];
                var fa = filters.i.all.csv.formatArray;
                if (b == 0x0A) { //line feed
                    var la = filters.i.all.csv.line.join('').replace(/;/g,' ').replace(/,/g,' ').replace(/\s+/g,' ').trim().split(' ');
                    filters.i.all.csv.line = []; //clear line buffer
//println('Received line: "' + la + '"');
                    var na = [filters.i.all.csv.lines];
                    for (var i in la) na.push( parseFloat( la[i] ));
//println('Parsed line: [' + na.join(',') + ']');
//print('Format: '); for (var i in fa) print( fa[i].join(',') + ';' ); println();
                    for (var cn=0; cn<fa.length; cn++) {
//println('++Adding point to series ' + cn + ' at ' + na[fa[cn][0]] + ',' + na[fa[cn][1]]);
                        if (!isNaN(na[fa[cn][0]]) && !isNaN(na[fa[cn][0]])) {
                            filters.i.all.csv.chart.addPoint(cn, na[fa[cn][0]], na[fa[cn][1]]);
                        }
                    }
                    filters.i.all.csv.lines++;
                } else {
                    filters.i.all.csv.line.push(String.fromCharCode( ((b >= 0x20) && (b <= 127)) ? b : ' '));
                }
            }
        }
    },
    
    props: {
        enabled: { type:'bool', name:'Enabled', descr:'Enable text processing',
            get: function()  { return filters.i.all.csv.enabled; },
            set: function(b) {
            	if (b) {
            		filters.i.all.csv.lines = 0;
            		filters.i.all.csv.chart.clear();
        		}
        		filters.i.all.csv.enabled = (b == true);
    		}
        },

        clear: { type:'button', name:'Clear', descr:'Clear all charts data',
            update: function() {
                filters.i.all.csv.chart.clear();
                filters.i.all.csv.lines = 0; //reset lines counter
            }
        },
        
        sep: { type:'str', name:'Sep', descr:'CSV columns separator', width:1,
            get: function()  { return filters.i.all.csv.sep; },
            set: function(s) { filters.i.all.csv.sep = s; }
        },
        
        format: { type:'str', name:'Format', descr:'Chart format: x1,y1;x2,y2 (0 - row counter, 1..N - CSV column)', width:10,
            get: function()  { return filters.i.all.csv.format; },
            set: function(s) {
//print('Set format: ' + s);
                //set and parse new format
                filters.i.all.csv.format = s;
                filters.i.all.csv.chart.setChartsNum( filters.i.all.csv.parseFormat().length );
                filters.i.all.csv.lines = 0; //reset lines counter
            }
        }
    }
};

