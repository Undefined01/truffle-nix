class Random {
  constructor() {
    this.seed = 74755;
  }

  next() {
    this.seed = (this.seed * 1309 + 13849) & 65535;
    return this.seed;
  }
}

class Ball {
  constructor(random) {
    this.x = random.next() % 500;
    this.y = random.next() % 500;
    this.xVel = (random.next() % 300) - 150;
    this.yVel = (random.next() % 300) - 150;
  }

  bounce() {
    const xLimit = 500;
    const yLimit = 500;
    let bounced = false;

    this.x += this.xVel;
    this.y += this.yVel;

    if (this.x > xLimit) {
      this.x = xLimit; this.xVel = 0 - Math.abs(this.xVel); bounced = true;
    }

    if (this.x < 0) {
      this.x = 0; this.xVel = Math.abs(this.xVel); bounced = true;
    }

    if (this.y > yLimit) {
      this.y = yLimit; this.yVel = 0 - Math.abs(this.yVel); bounced = true;
    }

    if (this.y < 0) {
      this.y = 0; this.yVel = Math.abs(this.yVel); bounced = true;
    }

    return bounced;
  }
}

function main() {
  const random = new Random();
  const ballCount = 100;
  let bounces = 0;
  const balls = new Array(ballCount);
  let i = 0;

  for (i = 0; i < ballCount; i += 1) {
    balls[i] = new Ball(random);
  }

  for (i = 0; i < 50; i += 1) {
    for (let j = 0; j < ballCount; j += 1) {
      if (balls[j].bounce()) {
        bounces += 1;
      }
    }
  }

  return bounces;
}