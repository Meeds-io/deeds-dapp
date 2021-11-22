const Snapshot = { template: '<deeds-snapshot />' };
const Stake = { template: '<deeds-stake />' };
const Deeds = { template: '<deeds-deeds />' };

export default {
  '/': Snapshot,
  '/snapshot': Snapshot,
  '/stake': Stake,
  '/deeds': Deeds,
};