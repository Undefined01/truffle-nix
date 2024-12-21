let
  mod = x: y: x - y * (x / y);

  seed = 74755;
  random = seed: mod (seed * 1309 + 13849) 65536;

  ballCount = 100;
  stepCount = 50;

  initialBalls = builtins.foldl' ({ seed, list }: _: 
      let
        x' = random seed;
        y' = random x';
        xVel' = random y';
        yVel' = random xVel';
        nextSeed = yVel';
      in
        {
          seed = nextSeed;
          list = list ++ [{
            x = mod x' 500;
            y = mod y' 500;
            xVel = (mod xVel' 300) - 150;
            yVel = (mod yVel' 300) - 150;
          }];
        }) { seed = seed; list = []; } (builtins.genList (_: 0) ballCount);

  step = { x, y, xVel, yVel }:
    let
      xLimit = 500;
      yLimit = 500;
      nextX = x + xVel;
      nextY = y + yVel;
      l = nextX < 0;
      r = nextX > xLimit;
      u = nextY < 0;
      d = nextY > yLimit;
      bounced = l || r || u || d;
      x' = if l then 0 else if r then xLimit else nextX;
      y' = if u then 0 else if d then yLimit else nextY;
      xVel' = if l || r then -xVel else xVel;
      yVel' = if u || d then -yVel else yVel;
    in
    {
      pos = { x = x'; y = y'; xVel = xVel'; yVel = yVel'; };
      bounced = bounced;
    };
in
{
  step = step;
  main = _: (builtins.foldl' (
      { balls, bounces }: _: 
        let
          stepResult = builtins.map (ball: step ball) balls;
          balls' = builtins.map (result: result.pos) stepResult;
          bounces' = bounces + (builtins.foldl' (acc: result: acc + (if result.bounced then 1 else 0)) 0 stepResult);
        in
          { balls = balls'; bounces = bounces'; }
    ) { balls = initialBalls.list; bounces = 0; } (builtins.genList (n: n) stepCount)).bounces;
}
