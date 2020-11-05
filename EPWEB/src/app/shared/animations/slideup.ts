import { trigger, transition, style, animate } from '@angular/animations';

export let slideIn = trigger('slideIn', [
    //state('small', style({ 'backgroundColor': 'green' })),
   // state('large', style({ 'backgroundColor': 'yellow' })),
   transition(":enter",[
    style({backgroundColor:"#ffffff", transform: "scale(0.22)"}),
    animate(500)
  ]),

    transition("small => large",[
      style({backgroundColor:"#ffffff", transform: "scale(0.22)"}),
      animate(500)
    ]),
    transition("large => small",[
      style({backgroundColor:"#ffffff", transform: "scale(0.22)"}),
      animate(500)
    ])
    
    //transition('small => large',  animate('3000ms')),
    //transition('large => small', animate('1000ms')),
  ])