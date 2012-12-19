// CAST: Dropdown Menu

// Base structure of code according to: http://docs.jquery.com/Plugins/Authoring (as of 09/12/2012)

/*
// calls the init method
$('.navMenu').CAST_Dropdown_Menu();

// calls the init method - setting sub menu indicator and forcing mobile style actions
$('.navMenu').CAST_Dropdown_Menu({
    subMark: "--",
    isMobile : true
});

// Callback definitions
create      - menu initialization complete

*/

(function($) {
    // Mobile browser detection
    var $isMobile = false;
    /*
    if( /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent) ) {
        $isMobile = true;
    }
    */
    try {
        document.createEvent('TouchEvent');
        $isMobile = true;
    } catch(e) {}

    // Menu object holder
    var menu = null;

    var c = {
        // Class names
        'hasSubMenu'        : "show-sub",
        'showSubMenu'       : "show-menu",
        'showHover'         : "show-hover",
        'isMobile'          : "isMobile",
    };

    var settings = {
        // Default Settings
        'subMark'           : "&gt;",       // Text character to use to indicate presence of sub-menu (overrule in CSS for graphical one)
        'delayHide'         : 500,          // Delay for hiding menu
        'navClick'          : false,        // Force mobile (click) style navigation

        // Callback hooks
        'create'            : null,
    };

    var methods = {
        // Initialize
        init : function(options) {
            settings = $.extend(settings, options);
            settings.isMobile = $isMobile   // Mobile browser flag - override not allowed

            // Set menu holder
            menu = $(this);

            // Indicate ARIA roles for top level list as menu, and each anchor as menuitem
            $(this).attr('role', 'menu');
            $('a', this).attr('role', 'menuitem');

            // Check for sub menu items and add indicator
            $(this).find('ul').each(function() {
                $(this).attr({
                    'role': 'menu',
                    'aria-expanded': 'false',
                    'aria-hidden': 'true'
                })
                .closest('li').addClass(c.hasSubMenu)
                .find('a').eq(0).attr('aria-haspopup','true')
                .append(' <span class="sub-mark">' + settings.subMark + '</span>');
            });

            // Set tabIndex=-1 so that sub-menu links can't receive focus until menu is open (skip top level items)
            $(this).find('ul a').attr('tabIndex',-1);

            // Mobile OFF - handle keyboard navigation and mouse hover
            if (!settings.isMobile && !settings.navClick) {
                methods.navEnableHover();
            }

            // Mobile ON - handle click/tap style navigation
            if (settings.isMobile || settings.navClick) {
                methods.navEnableClick();
            }

            // Hide menu if click or focus occurs outside of navigation
            $(this).find('a').last().keydown(function(e){
                if(e.keyCode == 9) {
                    // If the user tabs out of the navigation hide all menus
                    methods.hideMenu();
                }
            });
            $(document).click(function() {
                methods.hideMenu();
            });

            $(this).click(function(e){
                e.stopPropagation();
            });

            methods._trigger('create');

            return this;
        },

        /**
         * Mode: hover
         * Event: focus
         * Show submenu if needed when using keyboard navigation
         */
        _actionsHover_focus : function(e, n) {
            methods.showMenu(n);
        },

        /**
         * Mode: hover
         * Event: mouseenter/mouseleave
         * Handle mouse based events
         */
        _actionsHover_mouseenter : function(e, n) {
            if (menu.timerHide) clearTimeout(menu.timerHide);
            methods.showMenu(n);
        },
        _actionsHover_mouseleave : function(e, n) {
            if (menu.timerHide) clearTimeout(menu.timerHide);
            menu.timerHide = setTimeout(function() {
                menu.timerHide = null;
                methods.hideMenu();
            }, settings.delayHide);
        },

        /**
         * Mode: click (mobile)
         * Event: click
         * Handle click events
         */
        _actionsClick_click : function(e, n) {
            // Override click action if sub-menu present and not shown
            // Check class, not visibilty - item is visible, just not in viewport
            if ($(n).closest('li').hasClass(c.hasSubMenu) && !$(n).closest('li').hasClass(c.showHover)) {
                e.stopPropagation();
                e.preventDefault();
                methods.showMenu(n);
            } else {
                // Hide menu after click
                methods.hideMenu();
            }
        },

        /**
         * Disable Mode: click (mobile)
         * Enable Mode: hover
         */
        navEnableHover : function() {
            methods.navDisableClick();
            menu.find('a').on("focus.modeHover", function(event) {
                methods._actionsHover_focus(event, this);
            });
            menu.find('a').on("mouseenter.modeHover", function(event) {
                methods._actionsHover_mouseenter(event, this);
            });
            menu.find('a').on("mouseleave.modeHover", function(event) {
                methods._actionsHover_mouseleave(event, this);
            });
        },

        /**
         * Disable Mode: hover
         * Enable Mode: click (mobile)
         */
        navEnableClick : function() {
            methods.navDisableHover();
            menu.addClass(c.isMobile);
            menu.find('a').on("click.modeClick", function(event) {
                methods._actionsClick_click(event, this);
            });
        },

        /**
         * Disable Mode: hover
         */
        navDisableHover : function() {
            menu.find('a').off(".modeHover");
        },

        /**
         * Disable Mode: hover
         */
        navDisableClick : function() {
            menu.removeClass(c.isMobile);
            menu.find('a').off(".modeClick");
        },

        /**
         * Show sub-menu next to a given link ('a') node
         */
        showMenu : function(node) {
            $(node).closest('ul')
                .find('.' + c.showSubMenu).removeClass(c.showSubMenu)
                .attr({
                    'aria-expanded': 'false',
                    'aria-hidden': 'true'
                })
                .find('a').attr('tabIndex',-1);
            $(node).closest('li')
                .find('ul').eq(0).addClass(c.showSubMenu)
                .attr({
                    'aria-expanded': 'true',
                    'aria-hidden': 'false'
                })
                .find('a').attr('tabIndex',0);

            $(node).closest('ul').find('.' + c.showHover).removeClass(c.showHover);
            $(node).closest('ul').find('.' + c.showSubMenu).closest('li').addClass(c.showHover);
        },

        /**
         * Hide any open sub-menus
         */
        hideMenu : function() {
            menu.find('.' + c.showHover).removeClass(c.showHover);
            menu.find('.' + c.showSubMenu).removeClass(c.showSubMenu)
                .attr({
                    'aria-expanded': 'false',
                    'aria-hidden': 'true'
                })
                .find('a').attr('tabIndex',-1);
        },

        /**
         * Callback trigger function
         *
         * @param {Object} callback function name
         */
        _trigger : function(callback, node) {

            // Restrict informtion that is sent back
            var options = {
                'node'              : node,
                'isMobile'          : settings.isMobile,
                'navClick'          : settings.navClick,
            };

            if ($.isFunction(settings[callback])) {
                settings[callback](options);
            }
        },
    };

    $.fn.CAST_Dropdown_Menu = function(method) {
        // Method calling logic
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || ! method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' +  method + ' does not exist on jQuery.CAST_Dropdown_Menu');
        }
    };

})(jQuery);