# Concurrency TCK Distribution

## Generate Documentation

The Concurrency TCK has condensed the documentation down to a single asciidoc file
under `src/main/asciidoc/concurrency-tck-reference-guide.adoc`.  

This change will help with maintainability, readability, and distribution of the documentation to users. 

This project will output two different distributions of the TCK documentation. 
One is a PDF and the other is a static HTML page. 
To re-generate the TCK documentation run the following maven task: 

```sh
cd tck-dist
mvn generate-resources
```

The output documentation files will be located under `/tck-dist/target/generated-docs/`

## Documentation auto-refresh

When making edits to the documentation it is helpful to view the asciidoc and HTML output page together.
To accomplish this goal run the following maven task: 

```sh
cd tck-dist
mvn asciidoctor:http asciidoctor:auto-refresh
```

Then point your browser to `http://localhost:2000/concurrency-tck-reference-guide-<TCK_VERSION>.html`

Whenever edits are made to the source document, this page will be updated. 

## Generate Distribution

This project is configured to output the distributed archive for the Concurrency TCK.
To generate this archive run the following maven task: 

```sh
cd tck-dist
mvn package
```

The distributed archive will be output to `/tck-dist/target/concurrency-tck-<TCK_VERSION>-dist.zip`

## Links

- [AsciiDoc User Guide](http://asciidoc.org/userguide.html)
- [Asciidoctor quick reference](http://asciidoctor.org/docs/asciidoc-syntax-quick-reference)

