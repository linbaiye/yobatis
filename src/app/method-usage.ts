
export class MethodUsage {
  name: string;
  brief: string;
  doc: string;
  returnValue: string;
  example: string;

  constructor(name: string, brief: string, doc: string, returnValue: string, example: string) {
    this.name = name;
    this.brief = brief;
    this.doc = doc;
    this.returnValue = returnValue;
    this.example = example;
  }
}
