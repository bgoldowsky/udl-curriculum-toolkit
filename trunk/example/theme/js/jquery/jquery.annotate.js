(function($) {

    $.fn.annotateImage = function(options) {
        /// <summary>
        ///     Creates annotations on the given image.
        ///     Images are loaded from the "getUrl" propety passed into the options.
        /// </summary>
        var opts = $.extend({}, $.fn.annotateImage.defaults, options);
        var image = this;

        this.image = this;
        this.mode = 'view';

        // Assign defaults
        this.getUrl = opts.getUrl;
        this.saveUrl = opts.saveUrl;
        this.deleteUrl = opts.deleteUrl;
        this.editable = opts.editable;
        this.useAjax = opts.useAjax;
        this.notes = opts.notes;
        this.viewAnnotations = opts.viewAnnotations;
        this.hoverShow = opts.hoverShow;
        this.imageID = this.attr("id");

        // Add the canvas
        this.canvas = $('<div class="image-annotate-canvas"><div class="image-annotate-view"></div><div class="image-annotate-edit"><div class="image-annotate-edit-area"><div class="image-annotate-edit-area-inner"></div></div></div></div>');
        this.canvas.children('.image-annotate-edit').hide();
        this.canvas.children('.image-annotate-view').hide();
        this.image.after(this.canvas);

        // Give the canvas and the container their size and background
        this.canvas.height(this.height());
        this.canvas.width(this.width());
        this.canvas.css('background-image', 'url("' + this.attr('src') + '")');
        this.canvas.children('.image-annotate-view, .image-annotate-edit').height(this.height());
        this.canvas.children('.image-annotate-view, .image-annotate-edit').width(this.width());

        // Add the behavior: hide/show the notes when hovering the picture
        this.canvas.hover(function() {
            if ($(this).children('.image-annotate-edit').css('display') == 'none') {
                if (image.hoverShow) {
                    $(this).children('.image-annotate-view').show();
                }
            }
        }, function() {
            if (!$(this).children('.image-annotate-view').hasClass('image-annotate-labels-on')) {
                $(this).children('.image-annotate-view').hide();
            }
        });

        this.canvas.children('.image-annotate-view').hover(function() {
            $(this).show();
        }, function() {
            if (!$(this).hasClass('image-annotate-labels-on')) {
                $(this).hide();
            }
        });

        // load the notes
        if (this.useAjax) {
            $.fn.annotateImage.ajaxLoad(this);
        } else {
            $.fn.annotateImage.load(this);
        }

        // Add the "Add a note" button
        if (this.editable) {
            this.button = $('<a href="#" class="image-annotate-add">' + lang['ANNOTATE_ADD_NOTE'] + '</a>');
            this.button.click(function() {
                $.fn.annotateImage.add(image);
                return false;
            });
            this.canvas.after(this.button);
        }

        // Check to see if 'view bar' exists
        //xxx


        // Add the label toggles
        this.toggles = $('<div class="image-annotate-toggle">' + lang['ANNOTATE_LABELS'] + ': <span class="image-annotate-label-show">' + lang['ANNOTATE_SHOW'] + '</span> | <span class="image-annotate-label-hide">' + lang['ANNOTATE_HIDE'] + '</span></div>');
        this.canvas.before(this.toggles);
        this.toggles.width(this.width() - parseInt(this.toggles.css('padding-left')) - parseInt(this.toggles.css('padding-left')));
        // Stop hover effect for toggle bar
        this.toggles.hover(function(e) {
            e.stopPropagation();
        });
        $.fn.annotateImage.labelToggle(this);

        // Hide the original
        this.hide();

        return this;
    };

    /**
    * Plugin Defaults
    **/
    $.fn.annotateImage.defaults = {
        getUrl: 'your-get.rails',
        saveUrl: 'your-save.rails',
        deleteUrl: 'your-delete.rails',
        editable: false,
        useAjax: false,
        notes: new Array(),
        viewAnnotations: false,
        hoverShow: true
    };

    $.fn.annotateImage.clear = function(image) {
        /// <summary>
        ///     Clears all existing annotations from the image.
        /// </summary>
        for (var i = 0; i < image.notes.length; i++) {
            image.notes[image.notes[i]].destroy();
        }
        image.notes = new Array();
    };

    $.fn.annotateImage.ajaxLoad = function(image) {
        /// <summary>
        ///     Loads the annotations from the "getUrl" property passed in on the
        ///     options object.
        /// </summary>
        $.getJSON(image.getUrl + '?ticks=' + $.fn.annotateImage.getTicks(), function(data) {
            image.notes = data;
            $.fn.annotateImage.load(image);
        });
    };

    $.fn.annotateImage.load = function(image) {
        /// <summary>
        ///     Loads the annotations from the notes property passed in on the
        ///     options object.
        /// </summary>
        for (var i = 0; i < image.notes.length; i++) {
            image.notes[image.notes[i]] = new $.fn.annotateView(image, image.notes[i]);
        }
    };

    $.fn.annotateImage.getTicks = function() {
        /// <summary>
        ///     Gets a count og the ticks for the current date.
        ///     This is used to ensure that URLs are always unique and not cached by the browser.
        /// </summary>
        var now = new Date();
        return now.getTime();
    };

    $.fn.annotateImage.add = function(image) {
        /// <summary>
        ///     Adds a note to the image.
        /// </summary>
        if (image.mode == 'view') {
            image.mode = 'edit';

            // Create/prepare the editable note elements
            var editable = new $.fn.annotateEdit(image);

            $.fn.annotateImage.createSaveButton(editable, image);
            $.fn.annotateImage.createCancelButton(editable, image);
        }
    };

    $.fn.annotateImage.createSaveButton = function(editable, image, note) {
        /// <summary>
        ///     Creates a Save button on the editable note.
        /// </summary>
        var ok = $('<a class="image-annotate-edit-ok">' + lang['ANNOTATE_OK'] + '</a>');

        ok.click(function() {
            var form = $('#image-annotate-edit-form form');
            var text = $('#image-annotate-edit-text').val();
            $.fn.annotateImage.appendPosition(form, editable)
            image.mode = 'view';

            // Save via AJAX
            if (image.useAjax) {
                $.ajax({
                    url: image.saveUrl,
                    data: form.serialize(),
                    error: function(e) { alert(lang['ANNOTATE_ERROR_SAVE']) },
                    success: function(data) {
                if (data.annotation_id != undefined) {
                    editable.note.id = data.annotation_id;
                }
            },
                    dataType: "json"
                });
            }

            // Add to canvas
            if (note) {
                note.resetPosition(editable, text);
            } else {
                editable.note.editable = true;
                note = new $.fn.annotateView(image, editable.note)
                note.resetPosition(editable, text);
                image.notes.push(editable.note);
            }

            editable.destroy();
        });
        editable.form.append(ok);
    };

    $.fn.annotateImage.createCancelButton = function(editable, image) {
        /// <summary>
        ///     Creates a Cancel button on the editable note.
        /// </summary>
        var cancel = $('<a class="image-annotate-edit-close">' + lang['ANNOTATE_CANCEL'] + '</a>');
        cancel.click(function() {
            editable.destroy();
            image.mode = 'view';
        });
        editable.form.append(cancel);
    };

    $.fn.annotateImage.saveAsHtml = function(image, target) {
        var element = $(target);
        var html = "";
        for (var i = 0; i < image.notes.length; i++) {
            html += $.fn.annotateImage.createHiddenField("text_" + i, image.notes[i].text);
            html += $.fn.annotateImage.createHiddenField("top_" + i, image.notes[i].top);
            html += $.fn.annotateImage.createHiddenField("left_" + i, image.notes[i].left);
            html += $.fn.annotateImage.createHiddenField("height_" + i, image.notes[i].height);
            html += $.fn.annotateImage.createHiddenField("width_" + i, image.notes[i].width);
        }
        element.html(html);
    };

    $.fn.annotateImage.createHiddenField = function(name, value) {
        return '&lt;input type="hidden" name="' + name + '" value="' + value + '" /&gt;<br />';
    };

    $.fn.annotateEdit = function(image, note) {
        /// <summary>
        ///     Defines an editable annotation area.
        /// </summary>
        this.image = image;

        if (note) {
            this.note = note;
        } else {
            var newNote = new Object();
            newNote.id = "new";
            newNote.top = 30;
            newNote.left = 30;
            newNote.width = 30;
            newNote.height = 30;
            newNote.text = "";
            newNote.useImg = false;
            newNote.imgSrc = "";
            newNote.imgClass = "button annotate";
            this.note = newNote;
        }

        // Set area
        var area = image.canvas.children('.image-annotate-edit').children('.image-annotate-edit-area');
        this.area = area;
        this.area.css('height', this.note.height + 'px');
        this.area.css('width', this.note.width + 'px');
        this.area.css('left', this.note.left + 'px');
        this.area.css('top', this.note.top + 'px');
        //xxx
        this.area.find('.image-annotate-edit-area-inner').height((this.note.height - 1) + 'px');
        this.area.find('.image-annotate-edit-area-inner').width((this.note.width - 2) + 'px');

        // Show the edition canvas and hide the view canvas
        image.canvas.children('.image-annotate-view').hide();
        image.canvas.children('.image-annotate-edit').show();

        // Add the note (which we'll load with the form afterwards)
        var form = $('<div class="image-annotate-edit-form"><div class="image-annotate-edit-coords">Top: <span class="coords_top">' + this.note.top + '</span><br />Left: <span class="coords_left">' + this.note.left + '</span><br /></div><div class="image-annotate-edit-size">Width: <span class="coords_width">' + this.note.width + '</span><br />Height: <span class="coords_height">' + this.note.height + '</span></div><form></form></div>');
        this.form = form;

        $('body').append(this.form);
        this.form.css('left', this.area.offset().left + 'px');
        this.form.css('top', (parseInt(this.area.offset().top) + parseInt(this.area.height()) + 2) + 'px');

        // Set the area as a draggable/resizable element contained in the image canvas.
        // Would be better to use the containment option for resizable but buggy
        area.resizable({
            handles: 'all',
            resize: function(e, ui) {
                form.css('left', area.offset().left + 'px');
                form.css('top', (parseInt(area.offset().top) + parseInt(area.height()) + 2) + 'px');
                form.find('.coords_width').text(area.width());
                form.find('.coords_height').text(area.height());
                area.find('.image-annotate-edit-area-inner').height((area.height() - 1) + 'px');
                area.find('.image-annotate-edit-area-inner').width((area.width() - 2) + 'px');
            },
            stop: function(e, ui) {
                form.css('left', area.offset().left + 'px');
                form.css('top', (parseInt(area.offset().top) + parseInt(area.height()) + 2) + 'px');
                form.find('.coords_width').text(area.width());
                form.find('.coords_height').text(area.height());
                area.find('.image-annotate-edit-area-inner').height((area.height() - 1) + 'px');
                area.find('.image-annotate-edit-area-inner').width((area.width() - 2) + 'px');
            }
        })
        .draggable({
            containment: image.canvas,
            drag: function(e, ui) {
                form.css('left', area.offset().left + 'px');
                form.css('top', (parseInt(area.offset().top) + parseInt(area.height()) + 2) + 'px');
                form.find('.coords_top').html(area.position().top);
                form.find('.coords_left').html(area.position().left);
            },
            stop: function(e, ui) {
                form.css('left', area.offset().left + 'px');
                form.css('top', (parseInt(area.offset().top) + parseInt(area.height()) + 2) + 'px');
                form.find('.coords_top').html(area.position().top);
                form.find('.coords_left').html(area.position().left);
            }
        });
        return this;
    };

    $.fn.annotateEdit.prototype.destroy = function() {
        /// <summary>
        ///     Destroys an editable annotation area.
        /// </summary>
        this.image.canvas.children('.image-annotate-edit').hide();
        this.area.resizable('destroy');
        this.area.draggable('destroy');
        this.area.css('height', '');
        this.area.css('width', '');
        this.area.css('left', '');
        this.area.css('top', '');
        this.form.remove();
    }

    $.fn.annotateView = function(image, note) {
        /// <summary>
        ///     Defines a annotation area.
        /// </summary>
        this.image = image;

        this.note = note;

        this.editable = image.editable;

        // Add the area
        if (note.useImg) {
            this.area = $('<a href="#" class="image-annotate-area' + (this.editable ? ' image-annotate-area-editable' : '') + '"><div class="image-annotate-area-inner"><div class="' + note.imgClass + '"><img src="' + note.imgSrc + '" alt="" /><span class="image-annotate-text">' + note.text + '</span></div></div></a>');
        } else {
            this.area = $('<a href="#" class="image-annotate-area' + (this.editable ? ' image-annotate-area-editable' : '') + '"><div class="image-annotate-area-inner"><span class="image-annotate-text">' + note.text + '</span></div></a>');
        }
        image.canvas.children('.image-annotate-view').append(this.area);

        // Set the position and size of the area
        this.setPosition();

        // Add the behavior: hide/display the note when hovering the area
        var annotation = this;
        this.tooltip = null;

        this.area.hover(function() {
            annotation.show();
        }, function() {
            annotation.hide();
        });
        this.area.focusin(function() {
            annotation.show();
        });
        this.area.focusout(function() {
            annotation.hide();
        });

        // Edit a note feature
        if (image.editable) {
            var form = this;
            this.area.click(function() {
                form.edit();
                return false;
            });
        } else {
            this.area.click(function() {
                return $.fn.annotateCallback($(this), note.id);
            });
        }
    };

    $.fn.annotateView.prototype.setPosition = function() {
        /// <summary>
        ///     Sets the position of an annotation.
        /// </summary>
        this.area.children('div').height((parseInt(this.note.height) - 2) + 'px');
        this.area.children('div').width((parseInt(this.note.width) - 2) + 'px');
        this.area.css('left', (this.note.left) + 'px');
        this.area.css('top', (this.note.top) + 'px');
    };

    $.fn.annotateView.prototype.setPositionTooltip = function() {
        this.tooltip.css('left', (this.note.left) + 'px');
        this.tooltip.css('top', (parseInt(this.note.top) + parseInt(this.note.height) + 5) + 'px');
    };

    $.fn.annotateView.prototype.show = function() {
        /// <summary>
        ///     Highlights the annotation and shows the tooltip/note
        /// </summary>

        // Add the note/tooltip
        if (!this.image.editable) {
            if (this.tooltip == null) {
                this.tooltip = $('<div class="image-annotate-note">' + this.note.text + '</div>');
                //this.tooltip.hide();
                this.image.canvas.children('.image-annotate-view').append(this.tooltip);
                // Position note
                this.setPositionTooltip();
                // Display note
                this.tooltip.fadeIn(0);
            }
        }
        if (!this.editable) {
            this.area.addClass('image-annotate-area-hover');
        } else {
            this.area.addClass('image-annotate-area-editable-hover');
        }
    };

    $.fn.annotateView.prototype.hide = function() {
        /// <summary>
        ///     Removes the highlight from the annotation and hides the tooltip/note
        /// </summary>
        //this.tooltip.fadeOut(250);
        if (this.tooltip != null) { this.tooltip.remove(); }
        this.tooltip = null;
        this.area.removeClass('image-annotate-area-hover');
        this.area.removeClass('image-annotate-area-editable-hover');
    };

    $.fn.annotateView.prototype.destroy = function() {
        /// <summary>
        ///     Destroys the annotation.
        /// </summary>
        this.area.remove();
        if (this.tooltip != null) { this.tooltip.remove(); }
        if (this.form) { this.form.remove(); }
    }

    $.fn.annotateView.prototype.edit = function() {
        /// <summary>
        ///     Edits the annotation.
        /// </summary>
        if (this.image.mode == 'view') {
            this.image.mode = 'edit';
            var annotation = this;

            // Create/prepare the editable note elements
            var editable = new $.fn.annotateEdit(this.image, this.note);

            $.fn.annotateImage.createSaveButton(editable, this.image, annotation);

            // Add the delete button
            var del = $('<a class="image-annotate-edit-delete">' + lang['ANNOTATE_DELETE'] + '</a>');
            del.click(function() {
                var form = $('#image-annotate-edit-form form');

                $.fn.annotateImage.appendPosition(form, editable)

                if (annotation.image.useAjax) {
                    $.ajax({
                        url: annotation.image.deleteUrl,
                        data: form.serialize(),
                        error: function(e) { alert(lang['ANNOTATE_ERROR_DELETE']) }
                    });
                }

                annotation.image.mode = 'view';
                editable.destroy();
                annotation.destroy();
            });
            editable.form.append(del);

            $.fn.annotateImage.createCancelButton(editable, this.image);
        }
    };

    $.fn.annotateImage.appendPosition = function(form, editable) {
        /// <summary>
        ///     Appends the annotations coordinates to the given form that is posted to the server.
        /// </summary>
        var areaFields = $('<input type="hidden" value="' + editable.area.height() + '" name="height"/>' +
                           '<input type="hidden" value="' + editable.area.width() + '" name="width"/>' +
                           '<input type="hidden" value="' + editable.area.position().top + '" name="top"/>' +
                           '<input type="hidden" value="' + editable.area.position().left + '" name="left"/>' +
                           '<input type="hidden" value="' + editable.note.id + '" name="id"/>');
        form.append(areaFields);
    }

    $.fn.annotateView.prototype.resetPosition = function(editable, text) {
        /// <summary>
        ///     Sets the position of an annotation.
        /// </summary>

        // Resize
        this.area.children('div').height(editable.area.height() + 'px');
        this.area.children('div').width((editable.area.width() - 2) + 'px');
        this.area.css('left', (editable.area.position().left) + 'px');
        this.area.css('top', (editable.area.position().top) + 'px');

        // Save new position to note
        this.note.top = editable.area.position().top;
        this.note.left = editable.area.position().left;
        this.note.height = editable.area.height();
        this.note.width = editable.area.width();
        this.note.text = text;
        this.note.id = editable.note.id;
        this.editable = true;
    };

    $.fn.annotateImage.labelToggle = function(image) {
        if (image.viewAnnotations) {
            // Turn labels off
            image.toggles.children('.image-annotate-label-show').replaceWith('<span class="image-annotate-label-show">' +  lang['ANNOTATE_SHOW'] + '</span>');
            image.toggles.children('.image-annotate-label-hide').replaceWith('<a href="#" class="image-annotate-label-hide">' +  lang['ANNOTATE_HIDE'] + '</a>');
            image.toggles.children('.image-annotate-label-hide').click(function() {
                return $.fn.annotateImage.labelToggle(image);
            });
            if (image.canvas.children('.image-annotate-edit').css('display') == 'none') {
                image.viewAnnotations = false;
                image.canvas.children('.image-annotate-view').addClass('image-annotate-labels-on').show();
            }
        } else {
            // Turn labels on
            image.toggles.children('.image-annotate-label-show').replaceWith('<a href="#" class="image-annotate-label-show">' +  lang['ANNOTATE_SHOW'] + '</a>');
            image.toggles.children('.image-annotate-label-hide').replaceWith('<span class="image-annotate-label-hide">' +  lang['ANNOTATE_HIDE'] + '</span>');
            image.toggles.children('.image-annotate-label-show').click(function() {
                image.viewAnnotations = true;
                return $.fn.annotateImage.labelToggle(image);
            });
            image.canvas.children('.image-annotate-view').removeClass('image-annotate-labels-on').hide();
        }
        return false;
    };

    $.fn.annotateCallback = function(hotSpotNode, id) {
    	// determine if you are a graphic home page image

    	// show the hidden annotation relative to the hotspot that called it
    	var detail = $("#" + id);
    	var offset = hotSpotNode.offset();

		detail.css("top", offset.top);
		detail.css("left", offset.left);
		detail.show();
        return false;
    }

})(jQuery);