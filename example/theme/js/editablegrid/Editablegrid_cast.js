var myObject;

function cwmImportGrid(divId, textAreaId, url, readonly) {
	
    editableGrid = new EditableGrid("DemoGrid");     
	editableGrid.tableLoaded = function() { 
		if(readonly==true) {
			for ( i=0; i < editableGrid.getColumnCount(); i++) {
				editableGrid.getColumn(i).editable = false;												
			}			
		}

		this.renderGrid(divId, "dataTable"); 
	}; //testgrid is class of table
	editableGrid.loadJSON(url);
	this.initializeGrid();

	//load string into var
	var data = '{"metadata":[	{"name":"c1","label":"Column 1","datatype":"string","editable":true},	{"name":"c2","label":"Column 2","datatype":"string","editable":true},	{"name":"c3","label":"Column 3","datatype":"string","editable":true},	{"name":"c4","label":"Column 4","datatype":"string","editable":true},	{"name":"c5","label":"Column 5","datatype":"string","editable":true}],"data":[	{"id":1, "values":{"c1":"uk","c2":33,"c3":"Duke","c4":"Patience","c5":"1.842"}},	{"id":2, "values":{"c1":"uk","c2":33,"c3":"Duke","c4":"Patience","c5":"1.842"}},	{"id":3, "values":{"c1":"uk","c2":33,"c3":"Duke","c4":"Patience","c5":"1.842"}},	{"id":4, "values":{"c1":"uk","c2":33,"c3":"Duke","c4":"Patience","c5":"1.842"}},	{"id":5, "values":{"c1":"uk","c2":33,"c3":"Duke","c4":"Patience","c5":"1.842"}}]}';
	//parse string into object
	myObject = JSON.parse(data);
}

function cwmExportGrid(textAreaId) {
	jQuery('#'+textAreaId).text(JSON.stringify(myObject));  				
}

function initializeGrid() 
{
	with (this.editableGrid) {
		modelChanged = function(rowIndex, columnIndex, oldValue, newValue, row) { 
			myObject.data[rowIndex].values[myObject.metadata[columnIndex].name] = newValue;
		};				
	}
}

