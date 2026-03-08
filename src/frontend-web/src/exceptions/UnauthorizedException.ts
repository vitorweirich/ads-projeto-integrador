export class UnauthorizedException extends Error {
  public backTo?: string

  constructor(message: string, backTo?: string) {
    super(message)
    this.backTo = backTo
    this.name = 'UnauthorizedException'
  }
}
