import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/side-nav/src/vaadin-side-nav.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/app-layout/src/vaadin-app-layout.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import '@vaadin/icon/src/vaadin-icon.js';
import '@vaadin/side-nav/src/vaadin-side-nav-item.js';
import '@vaadin/context-menu/src/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/form-layout/src/vaadin-form-item.js';
import '@vaadin/grid/src/vaadin-grid.js';
import '@vaadin/grid/src/vaadin-grid-column.js';
import '@vaadin/grid/src/vaadin-grid-sorter.js';
import '@vaadin/checkbox/src/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/button/src/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/date-picker/src/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/form-layout/src/vaadin-form-layout.js';
import '@vaadin/dialog/src/vaadin-dialog.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/app-layout/src/vaadin-drawer-toggle.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/scroller/src/vaadin-scroller.js';
import '@vaadin/grid/src/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/notification/src/vaadin-notification.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'be46bad3b54e8be410bcdeb7b9d84bee7ca18e6e75220124e161a633b9596153') {
    pending.push(import('./chunks/chunk-f7b712d1159d08cbbee0fd61e29d5cd2c1d861bcdb3c407c3ebfe58c25ce09da.js'));
  }
  if (key === '33ff679a10caca7f21ee8057b989edfb6f558a240d20cff7b112c54e2a9623bb') {
    pending.push(import('./chunks/chunk-644eacd878b45d8533b867d5805fbd4cdbde78fb3789ec124b5e685da51ed592.js'));
  }
  if (key === 'a452a31bedb98686516525763fa8a3ce1d765f4d2993171caf8608906ad297d0') {
    pending.push(import('./chunks/chunk-644eacd878b45d8533b867d5805fbd4cdbde78fb3789ec124b5e685da51ed592.js'));
  }
  if (key === '5be997f4cfc9a509e9b4554c63258a1c89ddbedbefd06c1b4b67bb2e8050543d') {
    pending.push(import('./chunks/chunk-644eacd878b45d8533b867d5805fbd4cdbde78fb3789ec124b5e685da51ed592.js'));
  }
  if (key === '9452e34f287684f8201ed1988e23df4a13cd85fa700430ebb7a5089e2df685df') {
    pending.push(import('./chunks/chunk-f7b712d1159d08cbbee0fd61e29d5cd2c1d861bcdb3c407c3ebfe58c25ce09da.js'));
  }
  if (key === 'a471293854b05393423106a0a6e08705f8106aef13390071be1c9a0278acae7f') {
    pending.push(import('./chunks/chunk-483d6586a7bb601aebd718fcb8b0acbb58af1593abe66a07614c3014103f8e3e.js'));
  }
  if (key === 'cb17c778a68be28bfece5b30047b8bd02d303bc15536193cc5e6f9b94d612c94') {
    pending.push(import('./chunks/chunk-483d6586a7bb601aebd718fcb8b0acbb58af1593abe66a07614c3014103f8e3e.js'));
  }
  if (key === 'c41ae4be9b71bb284696265fee2fdbc55750c56a0bb735648c864edf1339dad7') {
    pending.push(import('./chunks/chunk-4d4ae22b8a03f8ef5def46e5017663d0dc6e47e2cfe88504d48604cc17a24322.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}