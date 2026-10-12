import { Injectable } from '@nestjs/common';

export interface PaymentResult {
  success: boolean;
  provider: string;
  transactionId: string;
  amount: number;
}

export interface PaymentProvider {
  charge(amount: number): Promise<PaymentResult>;
}

@Injectable()
export class PaymentsService implements PaymentProvider {
  async charge(amount: number): Promise<PaymentResult> {
    return {
      success: true,
      provider: 'stub',
      transactionId: `stub_${Date.now()}`,
      amount,
    };
  }
}
