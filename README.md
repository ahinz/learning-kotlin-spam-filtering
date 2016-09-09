# Spam

```
Spam is a brand of canned precooked meat products made by Hormel Foods
Corporation. It was first introduced in 1937 and gained popularity
worldwide after its use during World War II.
```

## Uh... why?

I like Scala quite a bit and a friend of mine recommended I take a dip
into Kotlin-land so I figured why not. In particular the goal is to
check out the current state of Java, Kotlin, Spring, and Gradle

## Environment

If you're on Mac you'll need Gradle installed:

```
brew install gradle
```

## Training data

Training a Spam model requires two steps. The first is to download the
training data from
https://spamassassin.apache.org/publiccorpus/. There's a script in the
`scripts` folder that will download it to the `resources` directory for
you.

```bash
./scripts/get-training.sh

```

Now build the actual model from the data via:

```bash
gradle run
```

It also returns some stats about the model. If I were better with Gradle
it would be fun to pass in some training parameters.

## Running

```bash
gradle bootRun
```

### Running in Dev

You can rebuild classes in the background and `bootRun` will
automatically reload them:

```bash
;; Background the first one or run in two terminals
gradle -t classes &
gradle bootRun
```

## Routes

To test messages POST JSON to:

```
POST /is-spam
<message is body>

Returns:
{"probSpam": p }

Where p is in [0.0, 1.0] or -1.0 if there isn't enough data

```

### Testing Routes

Routes can easily be tested via `curl`:

```
;; Testing a Ham message
curl -XPOST --data "@src/main/resources/ham/0062.b675bdb7b9e2321dfe97e48037fe7782" http://localhost:8080/is-spam

;; Testing a Spam message
curl -XPOST --data "@src/main/resources/spam/00232.2d55046b9cf0b192ad6332545ef2a334" http://localhost:8080/is-spam
```

## License

Copyright © 2015 Adam Hinz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
