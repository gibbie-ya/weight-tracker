const finisherCircuit = {
  label: "Finisher Circuit x3",
  items: ["10 push-ups", "10 squat jumps", "10 mountain climbers"],
  rest: "No rest between exercises. 60 sec between rounds."
};

export const DAYS = [
  {
    id: "A",
    label: "Day A",
    type: "Upper Body",
    tag: "Push + Pull",
    accent: "#e94560",
    exercises: [
      {
        id: "A1", name: "Barbell Bench Press",
        phases: [
          { sets: 4, reps: "8-10", note: "Straight sets. Focus on tempo and form." },
          { sets: 4, reps: "8-10", note: "Superset with A2. 30-45 sec rest within pair." },
          { sets: 4, reps: "6-8",  note: "Superset with A2. Final set to 1-2 reps shy of failure.", dropSet: true },
        ]
      },
      {
        id: "A2", name: "Barbell Bent-Over Row",
        phases: [
          { sets: 4, reps: "8-10", note: "Straight sets. Focus on tempo and form." },
          { sets: 4, reps: "8-10", note: "Superset with A1. 30-45 sec rest within pair." },
          { sets: 4, reps: "6-8",  note: "Superset with A1. Final set to 1-2 reps shy of failure.", dropSet: true },
        ]
      },
      {
        id: "A3", name: "Incline Dumbbell Press",
        phases: [
          { sets: 3, reps: "10-12", note: "Straight sets." },
          { sets: 3, reps: "10-12", note: "Superset with A4. 30-45 sec rest within pair." },
          { sets: 3, reps: "10-12", note: "Superset with A4." },
        ]
      },
      {
        id: "A4", name: "Face Pull",
        phases: [
          { sets: 3, reps: "15", note: "Straight sets." },
          { sets: 3, reps: "15", note: "Superset with A3. 30-45 sec rest within pair." },
          { sets: 3, reps: "15", note: "Superset with A3." },
        ]
      },
      {
        id: "A5", name: "Overhead Press",
        phases: [
          { sets: 3, reps: "8-10", note: "Straight sets." },
          { sets: 3, reps: "8-10", note: "Straight set." },
          { sets: 3, reps: "6-8",  note: "Final set to 1-2 reps shy of failure.", dropSet: true },
        ]
      },
      {
        id: "A6", name: "Lat Pulldown",
        phases: [
          { sets: 3, reps: "10-12", note: "Straight sets." },
          { sets: 3, reps: "10-12", note: "Straight set." },
          { sets: 3, reps: "8-10",  note: "Straight set." },
        ]
      },
      {
        id: "A7", name: "Tricep Pushdown",
        phases: [
          { sets: 2, reps: "12-15", note: "Straight sets." },
          { sets: 2, reps: "12-15", note: "Superset with A8. 30-45 sec rest within pair." },
          { sets: 2, reps: "12-15", note: "Superset with A8.", dropSet: true },
        ]
      },
      {
        id: "A8", name: "Dumbbell Curl",
        phases: [
          { sets: 2, reps: "12-15", note: "Straight sets." },
          { sets: 2, reps: "12-15", note: "Superset with A7. 30-45 sec rest within pair." },
          { sets: 2, reps: "12-15", note: "Superset with A7." },
        ]
      },
    ],
    phaseFinisher: [null, null, finisherCircuit]
  },
  {
    id: "B",
    label: "Day B",
    type: "Lower Body",
    tag: "Quad + Glute",
    accent: "#4a9eff",
    exercises: [
      {
        id: "B1", name: "Barbell Back Squat",
        phases: [
          { sets: 4, reps: "6-8",  note: "Straight sets." },
          { sets: 4, reps: "6-8",  note: "Straight sets." },
          { sets: 4, reps: "5-6",  note: "Final set to near failure.", dropSet: true },
        ]
      },
      {
        id: "B2", name: "Romanian Deadlift",
        phases: [
          { sets: 4, reps: "8-10", note: "Straight sets." },
          { sets: 4, reps: "8-10", note: "Straight sets." },
          { sets: 4, reps: "8-10", note: "Final set to near failure.", dropSet: true },
        ]
      },
      {
        id: "B3", name: "Leg Press",
        phases: [
          { sets: 3, reps: "12-15", note: "Straight sets." },
          { sets: 3, reps: "12-15", note: "Superset with B4." },
          { sets: 3, reps: "12-15", note: "Superset with B4. Near failure." },
        ]
      },
      {
        id: "B4", name: "Lying Leg Curl",
        phases: [
          { sets: 3, reps: "12-15", note: "Straight sets." },
          { sets: 3, reps: "12-15", note: "Superset with B3." },
          { sets: 3, reps: "12-15", note: "Superset with B3." },
        ]
      },
      {
        id: "B5", name: "Walking Lunge",
        phases: [
          { sets: 3, reps: "10 each", note: "Straight sets." },
          { sets: 3, reps: "10 each", note: "Straight sets." },
          { sets: 3, reps: "12 each", note: "Straight sets." },
        ]
      },
      {
        id: "B6", name: "Calf Raise (Seated)",
        phases: [
          { sets: 3, reps: "15-20", note: "Straight sets." },
          { sets: 3, reps: "15-20", note: "Straight sets." },
          { sets: 3, reps: "15-20", note: "Near failure.", dropSet: true },
        ]
      },
      {
        id: "B7", name: "Hanging Knee Raise",
        phases: [
          { sets: 3, reps: "12-15", note: "Straight sets." },
          { sets: 3, reps: "12-15", note: "Straight sets." },
          { sets: 3, reps: "15",    note: "Straight sets." },
        ]
      },
    ],
    phaseFinisher: [null, null, null]
  },
  {
    id: "C",
    label: "Day C",
    type: "Upper Body",
    tag: "Horizontal + Vertical",
    accent: "#a855f7",
    exercises: [
      {
        id: "C1", name: "Pull-Up / Weighted Pull-Up",
        phases: [
          { sets: 4, reps: "6-8",  note: "Straight sets." },
          { sets: 4, reps: "6-8",  note: "Straight sets." },
          { sets: 4, reps: "5-6",  note: "Final set to near failure.", dropSet: true },
        ]
      },
      {
        id: "C2", name: "Dumbbell Shoulder Press",
        phases: [
          { sets: 4, reps: "10-12", note: "Straight sets." },
          { sets: 4, reps: "10-12", note: "Superset with C3." },
          { sets: 4, reps: "8-10",  note: "Superset with C3. Final set near failure.", dropSet: true },
        ]
      },
      {
        id: "C3", name: "Seated Cable Row",
        phases: [
          { sets: 4, reps: "10-12", note: "Straight sets." },
          { sets: 4, reps: "10-12", note: "Superset with C2." },
          { sets: 4, reps: "10-12", note: "Superset with C2." },
        ]
      },
      {
        id: "C4", name: "Chest-Supported Row",
        phases: [
          { sets: 3, reps: "10-12", note: "Straight sets." },
          { sets: 3, reps: "10-12", note: "Straight sets." },
          { sets: 3, reps: "8-10",  note: "Near failure.", dropSet: true },
        ]
      },
      {
        id: "C5", name: "Dumbbell Lateral Raise",
        phases: [
          { sets: 3, reps: "15-20", note: "Straight sets." },
          { sets: 3, reps: "15-20", note: "Superset with C6." },
          { sets: 3, reps: "15-20", note: "Superset with C6.", dropSet: true },
        ]
      },
      {
        id: "C6", name: "Rear Delt Fly",
        phases: [
          { sets: 3, reps: "15-20", note: "Straight sets." },
          { sets: 3, reps: "15-20", note: "Superset with C5." },
          { sets: 3, reps: "15-20", note: "Superset with C5." },
        ]
      },
      {
        id: "C7", name: "Cable Tricep Overhead Extension",
        phases: [
          { sets: 2, reps: "12-15", note: "Straight sets." },
          { sets: 2, reps: "12-15", note: "Superset with C8." },
          { sets: 2, reps: "12-15", note: "Superset with C8.", dropSet: true },
        ]
      },
      {
        id: "C8", name: "Hammer Curl",
        phases: [
          { sets: 2, reps: "12-15", note: "Straight sets." },
          { sets: 2, reps: "12-15", note: "Superset with C7." },
          { sets: 2, reps: "12-15", note: "Superset with C7." },
        ]
      },
    ],
    phaseFinisher: [null, null, finisherCircuit]
  },
  {
    id: "D",
    label: "Day D",
    type: "Lower Body",
    tag: "Posterior Chain",
    accent: "#22c55e",
    exercises: [
      {
        id: "D1", name: "Trap Bar Deadlift",
        phases: [
          { sets: 4, reps: "5-6",   note: "Straight sets." },
          { sets: 4, reps: "5-6",   note: "Straight sets." },
          { sets: 4, reps: "4-5",   note: "Final set to near failure.", dropSet: true },
        ]
      },
      {
        id: "D2", name: "Bulgarian Split Squat",
        phases: [
          { sets: 3, reps: "8-10 each", note: "Straight sets." },
          { sets: 3, reps: "8-10 each", note: "Straight sets." },
          { sets: 3, reps: "8-10 each", note: "Straight sets." },
        ]
      },
      {
        id: "D3", name: "Hip Thrust",
        phases: [
          { sets: 4, reps: "10-12", note: "Straight sets." },
          { sets: 4, reps: "10-12", note: "Straight sets." },
          { sets: 4, reps: "10-12", note: "Near failure.", dropSet: true },
        ]
      },
      {
        id: "D4", name: "Leg Extension",
        phases: [
          { sets: 3, reps: "15", note: "Straight sets." },
          { sets: 3, reps: "15", note: "Superset with D5." },
          { sets: 3, reps: "15", note: "Superset with D5.", dropSet: true },
        ]
      },
      {
        id: "D5", name: "Nordic Hamstring Curl / Leg Curl",
        phases: [
          { sets: 3, reps: "8-12", note: "Straight sets." },
          { sets: 3, reps: "8-12", note: "Superset with D4." },
          { sets: 3, reps: "8-12", note: "Superset with D4." },
        ]
      },
      {
        id: "D6", name: "Standing Calf Raise",
        phases: [
          { sets: 3, reps: "15-20", note: "Straight sets." },
          { sets: 3, reps: "15-20", note: "Straight sets." },
          { sets: 3, reps: "15-20", note: "Near failure.", dropSet: true },
        ]
      },
      {
        id: "D7", name: "Ab Wheel Rollout",
        phases: [
          { sets: 3, reps: "10-12", note: "Straight sets." },
          { sets: 3, reps: "12",    note: "Straight sets." },
          { sets: 3, reps: "12-15", note: "Straight sets." },
        ]
      },
    ],
    phaseFinisher: [null, null, null]
  },
];
