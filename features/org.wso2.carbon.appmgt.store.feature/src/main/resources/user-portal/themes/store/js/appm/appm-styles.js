/**
 * @licence
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This file contains CSS style related JS stuff.
 */

/**
 * Registers the click event handlers for selectable dropdowns.
 *
 * Selectable dropdowns:
 * Add <code>.dropdown-selectable</code> CSS class to the dropdown menu (<code>.dropdown-menu</code>) and specify the
 * ID of the dropdown button which you want to change the text using <code>data-target</code> attribute.</p>
 * @example
 * <pre><code>
 *    <ul class="dropdown-menu dropdown-selectable" data-target="#my-dropdown-btn">
 *       <li><a href="#">Action</a></li>
 *       <li><a href="#">Another action</a></li>
 *    </ul>
 * </code></pre>
 *
 * @param elements {jQuery[]} selectable dropdowns
 */
function registerEventHandlersForSelectableDropdowns(elements) {
    var cartHtml = ' <span class="caret"></span>';

    var onClick = function (event) {
        var $this = $(this);
        var textChangeElement = event.data.textChangeElement;
        textChangeElement.html($this.text() + cartHtml);
        textChangeElement.data('value', $this.data('value'));
    };

    elements.each(function () {
        var $this = $(this);
        var target = $this.data('target');
        if (!target) {
            return;
        }
        var targetElement = $(target);
        $this.find("li").on('click', {textChangeElement: $(targetElement)}, onClick);
    });
}

$(document).ready(function () {

    if ($("#searchBtn").is(":visible")) {
        $("#page-header").addClass("top-extra");
    } else {
        $("#page-header").removeClass("top-extra");

    }
    registerEventHandlersForSelectableDropdowns($('.dropdown-selectable'));
});