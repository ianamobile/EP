import { trigger, transition, style, animate } from '@angular/animations';

export let slideUpDown = trigger('slideUpDown', [
    //state('small', style({ 'backgroundColor': 'green' })),
   // state('large', style({ 'backgroundColor': 'yellow' })),
   transition(":enter",[
   // style({backgroundColor:"#ffffff", transform: "scale(0.22)"}),
    style({backgroundColor:"#ffffff", transform: "translateY(150px)"}),
    animate(300)
  ]),

    transition("small => large",[
      style({backgroundColor:"#ffffff", transform: "scale(0.22)"}),
      animate(500)
    ]),
    transition("large => small",[
      style({backgroundColor:"#ffffff", transform: "scale(0.22)"}),
      animate(500)
    ]),
    transition(":leave",[
       // style({backgroundColor:"#ffffff", transform: "scale(0.22)"}),
        style({backgroundColor:"#ffffff", transform: "translateY(-10px)"}),
        animate(300)
      ])
    
    //transition('small => large',  animate('3000ms')),
    //transition('large => small', animate('1000ms')),
  ])